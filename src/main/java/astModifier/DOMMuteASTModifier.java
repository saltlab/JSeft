package astModifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.WhileLoop;
import com.crawljax.core.CrawljaxController;

import domMutation.Node;
import domMutation.NodeProperty;
import executionTracer.ProgramPoint;

public abstract class DOMMuteASTModifier implements NodeVisitor {
	private TreeMap<String, ArrayList<NodeProperty>> func_domNode_map;

	protected static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());
	
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
	protected DOMMuteASTModifier(TreeMap<String, ArrayList<NodeProperty>> func_domNode_map) {
		this.func_domNode_map=func_domNode_map;
		
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



	
	protected abstract AstNode createMutationNode(FunctionNode function, String xpath, String postfix, String accessType, String property);
	

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

	@Override
	public boolean visit(AstNode node) {
		

		if(node instanceof FunctionNode){
			FunctionNode func=(FunctionNode) node;
			String funcName=getFunctionName(func);
			ArrayList<NodeProperty> nodeProps=func_domNode_map.get(funcName);
			if(nodeProps!=null){
				for(int i=0;i<nodeProps.size();i++){
					NodeProperty nodeProp=nodeProps.get(i);
					Node domNode=nodeProp.getNode();
					String xpath=domNode.xpath;
					String line=nodeProp.getLine();
					String value=nodeProp.getValue();
					String typeofAccess=nodeProp.getTypeOfAccess();
					String property=nodeProp.getProperty();
					
					AstNode newNode=createMutationNode(func, xpath, ProgramPoint.ENTERPOSTFIX, typeofAccess, property);
					func.getBody().addChildToFront(newNode);

					/* get last line of the function */
					AstNode lastnode = (AstNode) func.getBody().getLastChild();
					/* if this is not a return statement, we need to add logging here also */
					if (!(lastnode instanceof ReturnStatement)) {
						newNode = createMutationNode(func, xpath, ProgramPoint.ENTERPOSTFIX, typeofAccess, property);
						/* add as last statement */
						func.getBody().addChildToBack(newNode);
						
					}
				}
					
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
				
				FunctionNode func = node.getEnclosingFunction();

				AstNode newNode = createMutationNode(func, (ReturnStatement)node, ProgramPoint.EXITPOSTFIX, node.getLineno());

				AstNode parent = makeSureBlockExistsAround(node);

				/* the parent is something we can prepend to */
				parent.addChildBefore(newNode, node);
							
			} 
		
		return true;
	}

	private AstNode getLineNode(AstNode node) {
		while ((!(node instanceof ExpressionStatement) && !(node instanceof Assignment))
		        || node.getParent() instanceof ReturnStatement) {
			node = node.getParent();
		}
		return node;
	}
	
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
	


}
