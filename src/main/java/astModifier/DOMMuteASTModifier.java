package astModifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawljaxController;

import domMutation.Node;
import domMutation.NodeProperty;
import executionTracer.ProgramPoint;

public abstract class DOMMuteASTModifier implements NodeVisitor {
	
	private NodeProperty nodeProp;
	private String funcName;
	private boolean shouldDeleteNode;
	private String stateName;

	protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());
	
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
	protected DOMMuteASTModifier(String funcName,NodeProperty nodeProp, boolean shouldDeleteNode, String stateName) {
	//	Set<Entry<String,ArrayList<NodeProperty>>> set=func_domNode_map.entrySet();
		this.funcName=funcName;
		this.nodeProp=nodeProp;
		this.shouldDeleteNode=shouldDeleteNode;
		this.stateName=stateName;
		
		
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
	//	System.out.print(code+"*******\n");
		return p.parse(code, null, 0);
		
	}



	
	protected abstract AstNode createMutationNode(FunctionNode function, String xpath, String postfix, String accessType, String property, boolean shouldDeleteNode);
	

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
			String functionName=getFunctionName(func);
			
			if(functionName.equals(funcName)){	
				Node domNode=nodeProp.getNode();
				String xpath=domNode.xpath;
				String line=nodeProp.getLine();
				String value=nodeProp.getValue();
				String typeofAccess=nodeProp.getTypeOfAccess();
				String property=nodeProp.getProperty();
				
				AstNode newNode=createMutationNode(func, xpath, ProgramPoint.ENTERPOSTFIX, typeofAccess, property,shouldDeleteNode);
				func.getBody().addChildToFront(newNode);

				/* get last line of the function */
				AstNode lastnode = (AstNode) func.getBody().getLastChild();
				/* if this is not a return statement, we need to add logging here also */
				if (!(lastnode instanceof ReturnStatement)) {
					newNode = createMutationNode(func, xpath, ProgramPoint.EXITPOSTFIX, typeofAccess, property,shouldDeleteNode);
					/* add as last statement */
					func.getBody().addChildToBack(newNode);
					
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
			 String functionName=getFunctionName(func);
			 
			 if(functionName.equals(funcName)){	
				 Node domNode=nodeProp.getNode();
				 String xpath=domNode.xpath;
				 String line=nodeProp.getLine();
				 String value=nodeProp.getValue();
				 String typeofAccess=nodeProp.getTypeOfAccess();
				 String property=nodeProp.getProperty();
				 AstNode newNode = createMutationNode(func, xpath, ProgramPoint.EXITPOSTFIX, typeofAccess, property,shouldDeleteNode);
				 
				 AstNode parent = makeSureBlockExistsAround(node);
				 
				 /* the parent is something we can prepend to */
				 parent.addChildBefore(newNode, node);
				 
			 } 
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
	
	public String getStateName(){
		return stateName;
	}

}
