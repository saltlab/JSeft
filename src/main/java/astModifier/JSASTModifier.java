package astModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.WhileLoop;

import com.crawljax.core.CrawljaxController;

import executionTracer.ProgramPoint;

public abstract class JSASTModifier implements NodeVisitor  {


	private final Map<String, String> mapper = new HashMap<String, String>();

	protected static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());
//	private static HashMap<String,TreeSet<String>> domProps=new HashMap<String,TreeSet<String>>();
	private ArrayList<String> nodesNotTolook=new ArrayList<String>();
	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;

	/**
	 * @param scopeName
	 *            the scopeName to set
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	/**
	 * @return the scopeName
	 */
	public String getScopeName() {
		return scopeName;
	}

	/**
	 * Abstract constructor to initialize the mapper variable.
	 */
	protected JSASTModifier() {
		nodesNotTolook.add("send(new Array(");
		nodesNotTolook.add("new Array(");
		nodesNotTolook.add("addVariable");

		/* add -<number of arguments> to also make sure number of arguments is the same */
		mapper.put("addClass", "attr('class')");
		mapper.put("removeClass", "attr('class')");
		mapper.put("css", "css(%0)");
		mapper.put("attr", "attr(%0)");
		mapper.put("prop", "attr(%0)");
		mapper.put("css-2", "css(%0)");
		mapper.put("attr-2", "attr(%0)");
		mapper.put("prop-2", "attr(%0)");
/*		mapper.put("append", "text()");
		mapper.put("after", "parent().html()");
		mapper.put("appendTo", "html()");
		mapper.put("before","parent().html()");
*/		mapper.put("detach", "html()");
		mapper.put("remove", "html()");
		mapper.put("empty", "html()");
		mapper.put("height-1", "height()");
		mapper.put("width-1", "width()");
		mapper.put("insertBefore", "prev().html()");
		mapper.put("insertAfter", "next().html()");
		mapper.put("offset-1", "offset()");
/*		mapper.put("prepend", "html()");
		mapper.put("prependTo", "html()");
*/		mapper.put("html-1", "html()");
		mapper.put("setAttribute-2", "getAttribute(%0)");
		mapper.put("text-1", "text()");
		mapper.put("getElementById", "getElementById(%0).length");
		mapper.put("getElementsByTagName", "getElementsByTagName(%0).length");
		
	//	mapper.put("className", "className");
		
	}

	/**
	 * Parse some JavaScript to a simple AST.
	 * 
	 * @param code
	 *            The JavaScript source code to parse.
	 * @return The AST node.
	 */
	public AstNode parse(String code) {
		Parser p = new Parser(compilerEnvirons, null);
		System.out.print(code+"*******\n");
		return p.parse(code, null, 0);
		
	}

	/**
	 * Find out the function name of a certain node and return "anonymous" if it's an anonymous
	 * function.
	 * 
	 * @param f
	 *            The function node.
	 * @return The function name.
	 */
	protected String getFunctionName(FunctionNode f) {
		if (f==null)
			return "NoFunctionNode";
	/*	else if(f.getParent() instanceof LabeledStatement){
			return ((LabeledStatement)f.getParent()).shortName();
		}
	*/	else if(f.getParent() instanceof ObjectProperty){
			return ((ObjectProperty)f.getParent()).getLeft().toSource();
		}
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
	}

	/**
	 * Creates a node that can be inserted at a certain point in function.
	 * 
	 * @param function
	 *            The function that will enclose the node.
	 * @param postfix
	 *            The postfix function name (enter/exit).
	 * @param lineNo
	 *            Linenumber where the node will be inserted.
	 * @return The new node.
	 */
	protected abstract AstNode createEnterNode(FunctionNode function, String postfix, int lineNo);
	protected abstract AstNode createExitNode(FunctionNode function, ReturnStatement returnNode, String postfix, int lineNo);
	/**
	 * Creates a node that can be inserted before and after a DOM modification statement (such as
	 * jQuery('#test').addClass('bla');).
	 * 
	 * @param shouldLog
	 *            The variable that should be logged (for example jQuery('#test').attr('style'))
	 * @param lineNo
	 *            The line number where this will be inserted.
	 * @return The new node.
	 */
	protected abstract AstNode createPointNode(FunctionNode function, String domNode, String shouldLog, int lineNo);

	/**
	 * Create a new block node with two children.
	 * 
	 * @param node
	 *            The child.
	 * @return The new block.
	 */
	private Block createBlockWithNode(AstNode node) {
		Block b = new Block();

		b.addChild(node);

		return b;
	}

	/**
	 * @param node
	 *            The node we want to have wrapped.
	 * @return The (new) node parent (the block probably)
	 */
	private AstNode makeSureBlockExistsAround(AstNode node) {
		AstNode parent = node.getParent();

		if (parent instanceof IfStatement) {
			/* the parent is an if and there are no braces, so we should make a new block */
			IfStatement i = (IfStatement) parent;

			/* replace the if or the then, depending on what the current node is */
			if (i.getThenPart().equals(node)) {
				i.setThenPart(createBlockWithNode(node));
			} else if (i.getElsePart()!=null){
				if (i.getElsePart().equals(node))
					i.setElsePart(createBlockWithNode(node));
			}
			
		} else if (parent instanceof WhileLoop) {
			/* the parent is a while and there are no braces, so we should make a new block */
			/* I don't think you can find this in the real world, but just to be sure */
			WhileLoop w = (WhileLoop) parent;
			if (w.getBody().equals(node))
				w.setBody(createBlockWithNode(node));
		} else if (parent instanceof ForLoop) {
			/* the parent is a for and there are no braces, so we should make a new block */
			/* I don't think you can find this in the real world, but just to be sure */
			ForLoop f = (ForLoop) parent;
			if (f.getBody().equals(node))
				f.setBody(createBlockWithNode(node));
		}

		return node.getParent();
	}

	/**
	 * Actual visiting method.
	 * 
	 * @param node
	 *            The node that is currently visited.
	 * @return Whether to visit the children.
	 */
/*	private boolean domPropAdded=false;
	private FunctionNode enclosedFunc = null;
	private String objAndFunc = "";
*/	@Override
	public boolean visit(AstNode node) {
		
		if(!shouldVisitNode(node))
			return false;
		FunctionNode func;

		

		if (node instanceof FunctionNode) {
			func = (FunctionNode) node;

			/* this is function enter */
			AstNode newNode = createEnterNode(func, ProgramPoint.ENTERPOSTFIX, func.getLineno());

			func.getBody().addChildToFront(newNode);

			/* get last line of the function */
			node = (AstNode) func.getBody().getLastChild();
			/* if this is not a return statement, we need to add logging here also */
			if (!(node instanceof ReturnStatement)) {
				newNode = createExitNode(func, null,ProgramPoint.EXITPOSTFIX, node.getLineno());
				/* add as last statement */
				func.getBody().addChildToBack(newNode);
				
			}

		} 
		
        else if (node instanceof SwitchCase) {
            //Add block around all statements in the switch case
            SwitchCase sc = (SwitchCase)node;
            List<AstNode> statements = sc.getStatements();
            List<AstNode> blockStatement = new ArrayList<AstNode>();
            Block b = new Block();
           
            if (statements != null) {
                Iterator<AstNode> it = statements.iterator();
                while (it.hasNext()) {
                    AstNode stmnt = it.next();
                    b.addChild(stmnt);
                }
               
                blockStatement.add(b);
                sc.setStatements(blockStatement);
            }
        }
		else if (node instanceof ReturnStatement) {
			
			func = node.getEnclosingFunction();

			AstNode newNode = createExitNode(func, (ReturnStatement)node, ProgramPoint.EXITPOSTFIX, node.getLineno());

			AstNode parent = makeSureBlockExistsAround(node);

			/* the parent is something we can prepend to */
			parent.addChildBefore(newNode, node);
			
			
/*			if(node.getEnclosingFunction().equals(enclosedFunc)){
			
				TreeSet<String> props=domProps.get(getFunctionName(enclosedFunc));
				if(props!=null){
					Iterator<String> iter=props.iterator();
					while(iter.hasNext()){
						String obj=iter.next();
						AstNode newnode=createPointNode(enclosedFunc,obj, node.getLineno() + 1);
						
						parent.addChildBefore(newnode, node);
			
					}
					domPropAdded=false;
					enclosedFunc=null;
						
				}
				
				
			}
*/			
		} 
/*		else if (node instanceof Name) {
			
			 lets detect function calls like .addClass, .css, .attr etc 
			if (node.getParent() instanceof PropertyGet
			        && node.getParent().getParent() instanceof FunctionCall && !node.getParent().toSource().contains("function")) {

				List<AstNode> arguments =
				        ((FunctionCall) node.getParent().getParent()).getArguments();

				String domNodeToLog;
	//			domPropAdded=false;
				if (mapper.get(node.toSource()) != null
				        || mapper.get(node.toSource() + "-" + arguments.size()) != null) {
					
					 this seems to be one! 
					PropertyGet g = (PropertyGet) node.getParent();
                    
					String objectAndFunction = mapper.get(node.toSource());
					if (objectAndFunction == null) {
						objectAndFunction = mapper.get(node.toSource() + "-" + arguments.size());
					}

					if (node.toSource().equals("appendTo") || node.toSource().equals("prependTo") || node.toSource().equals("insertAfter") || node.toSource().equals("insertBefore")){
						objectAndFunction="$"+"("+ arguments.get(0).toSource()+")"+"."+objectAndFunction;
						domNodeToLog=arguments.get(0).toSource();
					}
					else if (node.toSource().equals("before") || node.toSource().equals("after")) {
					    String leftMost=g.getLeft().toSource().replace(".before", "____").replace(".after", "____").split("____")[0];
					 
					    
						objectAndFunction= leftMost + "." + objectAndFunction;
						domNodeToLog=leftMost;
					}
			
					else{
						objectAndFunction = g.getLeft().toSource()+ "." + objectAndFunction;
						domNodeToLog=g.getLeft().toSource();
					
					}
					
					
					 fill in parameters in the "getter" 
						for (int i = 0; i < arguments.size(); i++) {
							objectAndFunction =
								objectAndFunction.replace("%" + i, arguments.get(i).toSource());
						}
					
					objectAndFunction=objectAndFunction.replace(" ", "____");
					AstNode parent = makeSureBlockExistsAround(getLineNode(node));
					DomNodeVisitor domNodeVisitor=new DomNodeVisitor(var);
		    		node.getEnclosingFunction().visit(domNodeVisitor);
		    		String domNode;
		    		if(domNodeVisitor.getDomNode()!=null)
		    			domNode=domNodeVisitor.getDomNode().toSource();
		    		else
		    			domNode=objectAndFunction;
					
					TreeSet<String> propSet=domProps.get(getFunctionName(node.getEnclosingFunction()));
					if(propSet!=null){
						domPropAdded=propSet.add(objectAndFunction);
					}
					else{
						propSet=new TreeSet<String>();
						domPropAdded=propSet.add(objectAndFunction);
						domProps.put(getFunctionName(node.getEnclosingFunction()), propSet);
					}
					enclosedFunc=node.getEnclosingFunction();
					objAndFunc=objectAndFunction;
					
					parent.addChildAfter(
					 createPointNode(node.getEnclosingFunction(),domNodeToLog, objectAndFunction, node.getLineno() + 1),
					 getLineNode(node));
                    
				}
			
				else
					if(node.toSource().equals("css")) 
						if(arguments.size()==1 && arguments.get(0).toSource().startsWith("{")) {
						
							PropertyGet g = (PropertyGet) node.getParent();
							String objectAndFunction="";
								
							objectAndFunction = g.getLeft().toSource().replace(" ", "____")+ "." + node.toSource();
							domNodeToLog=g.getLeft().toSource();
							
							String[] args=arguments.get(0).toSource().replace("{", "").replace("}","").split(",");
							for (int i=0; i<args.length; i++) {
						    	if (args[i].contains(":")){
						    		if (!args[i].split(":")[0].contains("'") && !args[i].split(":")[0].contains("\"")) {
						    			objectAndFunction+="(" + args[i].split(":")[0].replace(" ", "____")+ "'" + ")";
						    		}
						    		else {

						    			objectAndFunction+="(" + args[i].split(":")[0].replace(" ", "")+ ")";
						    		}	
						    		AstNode parent = makeSureBlockExistsAround(getLineNode(node));
						    		DomNodeVisitor domNodeVisitor=new DomNodeVisitor(var);
						    		node.getEnclosingFunction().visit(domNodeVisitor);
						    		String domNode;
						    		if(domNodeVisitor.getDomNode()!=null)
						    			domNode=domNodeVisitor.getDomNode().toSource();
						    		else
						    			domNode=objectAndFunction;
						    		
									TreeSet<String> propSet=domProps.get(getFunctionName(node.getEnclosingFunction()));
									if(propSet!=null){
										domPropAdded=propSet.add(objectAndFunction);
									}
									else{
										propSet=new TreeSet<String>();
										domPropAdded=propSet.add(objectAndFunction);
										domProps.put(getFunctionName(node.getEnclosingFunction()), propSet);
									}
									enclosedFunc=node.getEnclosingFunction();
									objAndFunc=objectAndFunction;
						    		parent.addChildAfter(
						    				createPointNode(node.getEnclosingFunction(), domNodeToLog, objectAndFunction, node.getLineno() + 1),
						    				getLineNode(node));
						    
									objectAndFunction = g.getLeft().toSource().replace(" ", "____")+ "." + node.toSource();
								
						    	}
							}
							
					}
						else
							if (node.toSource().equals("attr")) {
								if(arguments.size()==1 && arguments.get(0).toSource().startsWith("{")) {
									
									PropertyGet g = (PropertyGet) node.getParent();
									String objectAndFunction="";
										
									objectAndFunction = g.getLeft().toSource().replace(" ", "____")+ "." + node.toSource();
									domNodeToLog=g.getLeft().toSource();
									
									String[] args=arguments.get(0).toSource().replace("{", "").replace("}","").split(",");
									for (int i=0; i<args.length; i++) {
								    	if (args[i].contains(":")){

								   			objectAndFunction+="(" + args[i].split(":")[0].replace(" ", "") + ")";								    	
								    		AstNode parent = makeSureBlockExistsAround(getLineNode(node));
								    		DomNodeVisitor domNodeVisitor=new DomNodeVisitor(var);
								    		node.getEnclosingFunction().visit(domNodeVisitor);
								    		String domNode;
								    		if(domNodeVisitor.getDomNode()!=null)
								    			domNode=domNodeVisitor.getDomNode().toSource();
								    		else
								    			domNode=objectAndFunction;
								    	
								    		
											TreeSet<String> propSet=domProps.get(getFunctionName(node.getEnclosingFunction()));
											if(propSet!=null){
												domPropAdded=propSet.add(objectAndFunction);
											}
											else{
												propSet=new TreeSet<String>();
												domPropAdded=propSet.add(objectAndFunction);
												domProps.put(getFunctionName(node.getEnclosingFunction()), propSet);
											}
											enclosedFunc=node.getEnclosingFunction();
											objAndFunc=objectAndFunction;
								    		parent.addChildAfter(
								    				createPointNode(node.getEnclosingFunction(), domNodeToLog, objectAndFunction, node.getLineno() + 1),
								    				getLineNode(node));
								    		objectAndFunction = g.getLeft().toSource().replace(" ", "____")+ "." + node.toSource();
										
								    	}
									}
								}
							}
				
				if(domPropAdded){
					AstNode last = (AstNode) enclosedFunc.getBody().getLastChild();
					if(!(last instanceof ReturnStatement)){
						AstNode newnode=createPointNode(enclosedFunc,objAndFunc, last.getLineno() + 1);
						enclosedFunc.getBody().addChildToBack(newnode);
					}
					
					
				}
			
			}
		}*/
		
	


		/* have a look at the children of this node */
		return true;
	}

/*	private AstNode getLineNode(AstNode node) {
		while ((!(node instanceof ExpressionStatement) && !(node instanceof Assignment))
		        || node.getParent() instanceof ReturnStatement) {
			node = node.getParent();
		}
		return node;
	}
*/
	/**
	 * This method is called when the complete AST has been traversed.
	 * 
	 * @param node
	 *            The AST root node.
	 */
	public abstract void finish(AstRoot node);

	/**
	 * This method is called before the AST is going to be traversed.
	 */
	public abstract void start();
	
	private boolean shouldVisitNode(AstNode astnode){
		if (nodesNotTolook.size()==0)
			return true;
		String name="";
		if(astnode instanceof FunctionCall){
			
			FunctionCall funcCall=(FunctionCall) astnode;
			name=funcCall.getTarget().toSource();
		}
		else if(astnode instanceof FunctionNode){
			FunctionNode funcNode=(FunctionNode) astnode;
			name=funcNode.getName();
		}
		for (String node:nodesNotTolook){
			
			if (name.contains(node)){
				return false;
			}
		}
		
		return true;
	}
	

				
}
