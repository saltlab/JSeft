package astModifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
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
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.ThrowStatement;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.WhileLoop;

public class StatementCvgCalc  implements NodeVisitor{
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	@Override
	public boolean visit(AstNode node) {
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
	

	private AstNode createCovgArrayInitialization(FunctionNode func) {
		
		String funcName=getFunctionName(func);
		String code="stmCovgArray" + "[" + "'"+ funcName + "'" + "]" + "=0;";
		return parse(code);
	}


	private AstNode createCovgCalcNode(FunctionNode func) {
	
		String funcName=getFunctionName(func);
		String code="stmCovgArray" + "[" + "'"+ funcName + "'" + "]" + "++;";
		return parse(code);
	}

	
	private AstNode createCovgCalcNodeForCalledFunction(
			FunctionNode callerFunc, FunctionCall calleeFunc) {
		String callerFuncName=getFunctionName(callerFunc);
		String calleeFuncName=calleeFunc.getTarget().toSource();
		String code="stmCovgArray" + "[" + "'"+ callerFuncName + "'" + "]" + "= " + "stmCovgArray" + "[" + "'"+ callerFuncName + "'" + "]" + "stmCovgArray" + "[" + "'" + calleeFuncName + "'" +"];";
		return parse(code);
	}
		

	private AstNode parse(String code) {
		Parser p = new Parser(compilerEnvirons, null);
	//	System.out.print(code+"*******\n");
		return p.parse(code, null, 0);
		
	}
	
	private String getFunctionName(FunctionNode f) {
			
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
	
	private Block createBlockWithNode(AstNode node) {
		Block b = new Block();

		b.addChild(node);

		return b;
	}


}
