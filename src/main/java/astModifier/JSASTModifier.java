package astModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.ThrowStatement;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.WhileLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawljaxController;

import executionTracer.ProgramPoint;

public abstract class JSASTModifier implements NodeVisitor  {


	protected int numberOfDomRelatedNodes=0;
	private final Map<String, String> mapper = new HashMap<String, String>();

	protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());
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
		nodesNotTolook.add("stripScripts");

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
	*/	
		else if(f.getParent() instanceof ObjectProperty){
			return ((ObjectProperty)f.getParent()).getLeft().toSource();
		}
		

		else if(f.getParent() instanceof Assignment){
			AstNode funcAssignLeft=((Assignment) f.getParent()).getLeft();
			if(funcAssignLeft instanceof VariableDeclaration){
				return ((VariableDeclaration)funcAssignLeft).getVariables().get(0).toSource();
			}
			if(funcAssignLeft instanceof Name){
				return ((Name)funcAssignLeft).getIdentifier();
			}
			
			if(funcAssignLeft instanceof PropertyGet){
				if(((PropertyGet)funcAssignLeft).getLeft().toSource().equals("this")){
					String constructorName=f.getEnclosingFunction().getFunctionName().getIdentifier();
					String memberName=((PropertyGet)funcAssignLeft).getRight().toSource();
					String funcName="new " + constructorName + "()" + "." + memberName; 
					return(funcName);
				}
				
			}
				
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

	protected abstract AstNode createNodeToLogDomNodes(String domNode, String shouldLog);
	
	protected abstract AstNode createInstrumentationArrayLocalVariable();
	protected abstract AstNode createCovgArrayInitialization(FunctionNode function);
	protected abstract AstNode createCovgCalcNode(FunctionNode function);
	protected abstract AstNode createCovgCalcNodeForCalledFunction(FunctionNode callerFunc, FunctionCall calleeFunc);
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
		
		StatementCvgCalc stmCvgCalc=new StatementCvgCalc();
	
		node.visit(stmCvgCalc);
		if(!shouldVisitNode(node))
			return false;
		FunctionNode func;

		

		if (node instanceof FunctionNode) {
			func = (FunctionNode) node;

			/* this is function enter */
			AstNode instumentationArrayNode=createInstrumentationArrayLocalVariable();
			AstNode newNode = createEnterNode(func, ProgramPoint.ENTERPOSTFIX, func.getLineno());

			
			func.getBody().addChildToFront(instumentationArrayNode);
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
		else if (node instanceof Name) {
				
				
				if (node.getParent() instanceof PropertyGet
					        && node.getParent().getParent() instanceof FunctionCall && !node.getParent().toSource().contains("function")) {

						List<AstNode> arguments =
						        ((FunctionCall) node.getParent().getParent()).getArguments();

						String domNodeToLog;

						if (mapper.get(node.toSource()) != null
						        || mapper.get(node.toSource() + "-" + arguments.size()) != null) {
						
							PropertyGet g = (PropertyGet) node.getParent();
		                    
							String objectAndFunction = mapper.get(node.toSource());
							if (objectAndFunction == null) {
								objectAndFunction = mapper.get(node.toSource() + "-" + arguments.size());
							}


							if (node.toSource().equals("appendTo") || node.toSource().equals("prependTo") || node.toSource().equals("insertAfter") || node.toSource().equals("insertBefore")){
								domNodeToLog="$" + "(" + arguments.get(0).toSource() + ")";
								objectAndFunction="DIRECTACCESS";
								
							}

							else if(node.toSource().equals("children") || node.toSource().equals("parent")){
								domNodeToLog = g.getLeft().toSource()+ "." + objectAndFunction;
								objectAndFunction="DIRECTACCESS";
							}
					
							else{
								objectAndFunction = g.getLeft().toSource()+ "." + objectAndFunction;
								domNodeToLog=g.getLeft().toSource();
							
							}
							
							
						
								for (int i = 0; i < arguments.size(); i++) {
									objectAndFunction =
										objectAndFunction.replace("%" + i, arguments.get(i).toSource());
								}
							
							objectAndFunction=objectAndFunction.replace(" ", "____");
							AstNode parent = makeSureBlockExistsAround(getLineNode(node));		
							if(parent instanceof ReturnStatement){
								parent.addChildBefore(
										 createNodeToLogDomNodes(domNodeToLog, objectAndFunction),
										 getLineNode(node));
							}
							else
							parent.addChildAfter(
							 createNodeToLogDomNodes(domNodeToLog, objectAndFunction),
							 getLineNode(node));
							numberOfDomRelatedNodes++;
		                    
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
											if(parent instanceof ReturnStatement){
												parent.addChildBefore(
														 createNodeToLogDomNodes(domNodeToLog, objectAndFunction),
														 getLineNode(node));
											}
											else
												parent.addChildAfter(
												 createNodeToLogDomNodes(domNodeToLog, objectAndFunction),
												 getLineNode(node));
											numberOfDomRelatedNodes++;
								    
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
													if(parent instanceof ReturnStatement){
														parent.addChildBefore(
																 createNodeToLogDomNodes(domNodeToLog, objectAndFunction),
																 getLineNode(node));
													}
													else
														parent.addChildAfter(
														 createNodeToLogDomNodes(domNodeToLog, objectAndFunction),
														 getLineNode(node));
													numberOfDomRelatedNodes++;
													
										    		objectAndFunction = g.getLeft().toSource().replace(" ", "____")+ "." + node.toSource();
												
										    	}
											}
										}
									}
						
					}
				}
		        
		        else if(node instanceof FunctionCall){
					if( ((FunctionCall)node).getTarget() instanceof Name){
					
						if(((Name)((FunctionCall)node).getTarget()).getIdentifier().equals("$")
								|| ((Name)((FunctionCall)node).getTarget()).getIdentifier().equals("jQuery")){
						
							if(((FunctionCall)node).getArguments().size()==1
									&& ((FunctionCall)node).getArguments().get(0) instanceof StringLiteral){
								
								String domNodeToLog=node.toSource();
								String objectAndFunction="DIRECTACCESS";
								AstNode parent = makeSureBlockExistsAround(getLineNode(node));		
								if(parent instanceof ReturnStatement){
									parent.addChildAfter(
											 createNodeToLogDomNodes(domNodeToLog, objectAndFunction),
											 getLineNode(node));
								}
								else
									parent.addChildAfter(
									 createNodeToLogDomNodes(domNodeToLog, objectAndFunction),
									 getLineNode(node));
								numberOfDomRelatedNodes++;
					    	
								
							}
						}
						else if(((Name)((FunctionCall)node).getTarget()).getIdentifier().equals("getElementById") ||
								((Name)((FunctionCall)node).getTarget()).getIdentifier().equals("getElementsByTagName")){
							
							String domNodeToLog="document" + "." + node.toSource();
							String objectAndFunction="DIRECTACCESS";
							AstNode parent = makeSureBlockExistsAround(getLineNode(node));		
							if (parent instanceof ReturnStatement){
								parent.addChildAfter(
										 createNodeToLogDomNodes(domNodeToLog, objectAndFunction),
										 getLineNode(node));
							}
							else
								parent.addChildAfter(
										createNodeToLogDomNodes(domNodeToLog, objectAndFunction),
										getLineNode(node));
							numberOfDomRelatedNodes++;
				    	
							
						}
					}
		        }
		        
		

		
	


		/* have a look at the children of this node */
		return true;
	}

	private AstNode getLineNode(AstNode node) {
		while ((!(node instanceof ExpressionStatement) && !(node instanceof Assignment))
		        && !(node.getParent() instanceof ReturnStatement)) {
			node = node.getParent();
		}
		if(node.getParent() instanceof ReturnStatement)
			return node.getParent();
		return node;
	}

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
	
	@Deprecated
	private boolean visitNodesForCovgCalc(AstNode node)
	{
		
		FunctionNode func;
		
		if (!((node instanceof FunctionNode || node instanceof ReturnStatement || node instanceof SwitchCase || node instanceof AstRoot || node instanceof ExpressionStatement || node instanceof BreakStatement || node instanceof ContinueStatement || node instanceof ThrowStatement || node instanceof VariableDeclaration))) {// || node instanceof ExpressionStatement || node instanceof BreakStatement || node instanceof ContinueStatement || node instanceof ThrowStatement || node instanceof VariableDeclaration || node instanceof ReturnStatement || node instanceof SwitchCase)) {
			return true;
		}

		if (node instanceof FunctionNode) {
			func = (FunctionNode) node;

			/* this is function enter */
			AstNode covgArray=createCovgArrayInitialization(func);
			

			func.getBody().addChildToFront(covgArray);
			
			node = (AstNode) func.getBody().getFirstChild();
			node = (AstNode) node.getNext(); //The first node is the node just added in front, so get next node
			int firstLine = 0;
			if (node != null) {
				firstLine = node.getLineno();
			}

			/* get last line of the function */
			node = (AstNode) func.getBody().getLastChild();
			/* if this is not a return statement, we need to add logging here also */
			if (!(node instanceof ReturnStatement)) {
				AstNode newNode_end = createCovgCalcNode(func);
				/* add as last statement */
				func.getBody().addChildToBack(newNode_end);
			}			
			//System.out.println(func.toSource());
		}
		else if (node instanceof AstRoot) {
			AstRoot rt = (AstRoot) node;
			
			if (rt.getSourceName() == null) { //make sure this is an actual AstRoot, not one we created
				return true;
			}
			
			//this is the entry point of the AST root
			AstNode newNode = createCovgCalcNode(rt.getEnclosingFunction());

			rt.addChildToFront(newNode);
			
			node = (AstNode) rt.getFirstChild();
			node = (AstNode) node.getNext(); //The first node is the node just added in front, so get next node
			int firstLine = 0;
			if (node != null) {
				firstLine = node.getLineno();
			}
			
			// get last line of the function
			node = (AstNode) rt.getLastChild();
			//if this is not a return statement, we need to add logging here also
			if (!(node instanceof ReturnStatement)) {
				AstNode newNode_end = createCovgCalcNode(rt.getEnclosingFunction());
				//add as last statement
				rt.addChildToBack(newNode_end);
			}
		}
		//else if (node instanceof BreakStatement || node instanceof ConditionalExpression || node instanceof ContinueStatement || node instanceof ExpressionStatement || node instanceof FunctionCall || node instanceof Assignment || node instanceof InfixExpression || node instanceof ThrowStatement || node instanceof UnaryExpression || node instanceof VariableDeclaration || node instanceof VariableInitializer || node instanceof XmlDotQuery || node instanceof XmlMemberGet || node instanceof XmlPropRef || node instanceof Yield) {
		else if (node instanceof ExpressionStatement || node instanceof BreakStatement || node instanceof ContinueStatement || node instanceof ThrowStatement || node instanceof VariableDeclaration) {
			if (node instanceof VariableDeclaration) {
				//Make sure this variable declaration is not part of a for loop
				if (node.getParent() instanceof ForLoop) {
					return true;
				}
			}
			
			//Make sure additional try statement is not instrumented
			if (node instanceof TryStatement) {
				return true; //no need to add instrumentation before try statement anyway since we only instrument what's inside the blocks
			}
			
			
			func = node.getEnclosingFunction();
			
			if (func != null) {
				AstNode firstLine_node = (AstNode) func.getBody().getFirstChild();
				if (func instanceof FunctionNode && firstLine_node instanceof IfStatement) { //Perform extra check due to addition if statement
					firstLine_node = (AstNode) firstLine_node.getNext();
				}
				if (func instanceof FunctionNode && firstLine_node instanceof TryStatement) {
					TryStatement firstLine_node_try = (TryStatement) firstLine_node;
					firstLine_node = (AstNode) firstLine_node_try.getTryBlock().getFirstChild();
				}
				firstLine_node = (AstNode) firstLine_node.getNext();
				int firstLine = 0;
				if (firstLine_node != null) {
					//If first child is an ExpressionStatement or VariableDeclaration, then there might be multiple instances of the instrumented node at the beginning of the FunctionNode's list of children
					while (firstLine_node != null) {
						firstLine = firstLine_node.getLineno();
						if (firstLine > 0) {
							break;
						}
						else {
							firstLine_node = (AstNode) firstLine_node.getNext();
						}
					}
				}
				
				if (node.getLineno() >= firstLine) {
					AstNode newNode;
					if(node instanceof FunctionCall){
						 newNode= createCovgCalcNodeForCalledFunction(node.getEnclosingFunction(), (FunctionCall)node);
					}
						
					else{
						newNode = createCovgCalcNode(func);
					}
					
					//AstNode parent = node.getParent();
					
					AstNode parent = makeSureBlockExistsAround(node);
					
					//parent.addChildAfter(newNode, node);
					try {
						parent.addChildBefore(newNode, node);
					}
					catch (NullPointerException npe) {
						//System.out.println("Could not addChildBefore!");
						//System.out.println(npe.getMessage());
					}
				}
			}
			else { //The expression must be outside a function
				AstRoot rt = node.getAstRoot();
				if (rt == null || rt.getSourceName() == null) {
					return true;
				}
				AstNode firstLine_node = (AstNode) rt.getFirstChild();
				//if (firstLine_node instanceof IfStatement) { //Perform extra check due to addition if statement
				//	firstLine_node = (AstNode) firstLine_node.getNext();
				//}
				if (firstLine_node instanceof Block) {
					firstLine_node = (AstNode)firstLine_node.getFirstChild(); //Try statement
				}
				if (firstLine_node instanceof TryStatement) {
					TryStatement firstLine_node_try = (TryStatement) firstLine_node;
					firstLine_node = (AstNode) firstLine_node_try.getTryBlock().getFirstChild();
				}
				firstLine_node = (AstNode) firstLine_node.getNext();
				int firstLine = 0;
				if (firstLine_node != null) {
					//If first child is an ExpressionStatement or VariableDeclaration, then there might be multiple instances of the instrumented node at the beginning of the FunctionNode's list of children
					while (firstLine_node != null) {
						firstLine = firstLine_node.getLineno();
						if (firstLine > 0) {
							break;
						}
						else {
							firstLine_node = (AstNode) firstLine_node.getNext();
						}
					}
				}
				
				if (node.getLineno() >= firstLine) {
					AstNode newNode = createCovgArrayInitialization(rt.getEnclosingFunction());
					//AstNode parent = node.getParent();
					
					AstNode parent = makeSureBlockExistsAround(node);
					
					//parent.addChildAfter(newNode, node);
					try {
						parent.addChildBefore(newNode, node);
					}
					catch (NullPointerException npe) {
						System.out.println(npe.getMessage());
					}
				}
			}
		}
		else if (node instanceof ReturnStatement) {
			func = node.getEnclosingFunction();
			AstNode firstLine_node = (AstNode) func.getBody().getFirstChild();
			if (func instanceof FunctionNode && firstLine_node instanceof IfStatement) { //Perform extra check due to addition if statement
				firstLine_node = (AstNode) firstLine_node.getNext();
			}
			if (func instanceof FunctionNode && firstLine_node instanceof TryStatement) {
				TryStatement firstLine_node_try = (TryStatement) firstLine_node;
				firstLine_node = (AstNode) firstLine_node_try.getTryBlock().getFirstChild();
			}
			firstLine_node = (AstNode) firstLine_node.getNext();
			int firstLine = 0;
			if (firstLine_node != null) {
				//If first child is an ExpressionStatement or VariableDeclaration, then there might be multiple instances of the instrumented node at the beginning of the FunctionNode's list of children
				while (firstLine_node != null) {
					firstLine = firstLine_node.getLineno();
					if (firstLine > 0) {
						break;
					}
					else {
						firstLine_node = (AstNode) firstLine_node.getNext();
					}
				}
			}
			
			AstNode parent = makeSureBlockExistsAround(node);
			
			AstNode newNode = createCovgCalcNode(func);

			/* the parent is something we can prepend to */
			parent.addChildBefore(newNode, node);

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

		/* have a look at the children of this node */
		return true;
		
	}
				
}
