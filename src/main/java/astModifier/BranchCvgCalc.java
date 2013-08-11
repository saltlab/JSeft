package astModifier;

import java.util.List;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.WhileLoop;

public class BranchCvgCalc implements NodeVisitor {
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
	private FunctionNodeFinder funcFinder;

	public BranchCvgCalc(AstNode node){
		funcFinder=new FunctionNodeFinder();
		node.visit(funcFinder);
	}
	@Override
	public boolean visit(AstNode node) {

		
		if(node instanceof FunctionNode){
			FunctionNode func=(FunctionNode)node;
			String funcName=getFunctionName(func);
			AstNode covgArray=createBrnCovgArrayInitialization(func);

			func.getBody().addChildToFront(covgArray);
		}
		
		if(node instanceof FunctionCall){
		
			String funcCallName=((FunctionCall)node).getTarget().toSource();
			if(funcFinder.functionNodeNames.contains(funcCallName)){
				AstNode exp=node.getParent();
				while(!(exp instanceof ExpressionStatement) && !(exp instanceof IfStatement)
						&& !(exp instanceof WhileLoop) && !(exp instanceof ForLoop)
						&& !(exp instanceof SwitchCase)){
					exp=exp.getParent();
				}
				
				FunctionCall callee=(FunctionCall) node;
				FunctionNode caller=node.getEnclosingFunction();
				AstNode newNode=createAdjustBranchCovgAfterFuncCall(caller, callee);
				AstNode parent=makeSureBlockExistsAround(exp);
				parent.addChildAfter(newNode, exp);
				
				
			
			}
		}
			
		
		if(node instanceof IfStatement){
			IfStatement ifstm=(IfStatement) node;
			AstNode currentCondition=ifstm.getCondition();
			String newConditonSource="detectCoveredBranch"+"(" + currentCondition.toSource() + ", " + "'" + getFunctionName(node.getEnclosingFunction()) + "_" + ifstm.getLineno() + "'" +")";
			ExpressionStatement wrappedCondition=(ExpressionStatement) parse(newConditonSource).getFirstChild();
		
			ifstm.setCondition(wrappedCondition.getExpression());
		}
		
		else if(node instanceof WhileLoop){
			
				
			WhileLoop whilestm=(WhileLoop) node;
			AstNode currentCondition=whilestm.getCondition();
			String newConditonSource="detectCoveredBranch"+"("  + currentCondition.toSource() + ", " + "'" + getFunctionName(node.getEnclosingFunction()) + "_" + whilestm.getLineno() + "'" +")";
			ExpressionStatement wrappedCondition=(ExpressionStatement)parse(newConditonSource).getFirstChild();
			whilestm.setCondition(wrappedCondition.getExpression());
				
			
		}
		
		else if(node instanceof ForLoop){
			
			
			ForLoop forstm=(ForLoop) node;
			AstNode currentCondition=forstm.getCondition();
			String newConditonSource="detectCoveredBranch"+"(" + currentCondition.toSource() +  ", " + "'"  + getFunctionName(node.getEnclosingFunction()) + "_" + forstm.getLineno() + "'"  +")";
			System.out.println(newConditonSource);
			ExpressionStatement wrappedCondition=(ExpressionStatement)parse(newConditonSource).getFirstChild();
			forstm.setCondition(wrappedCondition.getExpression());
				
			
		}
		

		
		else if(node instanceof SwitchStatement){
			
			
			SwitchStatement switchstm=(SwitchStatement) node;
			List<SwitchCase> currentCases=switchstm.getCases();
			for(SwitchCase currCase:currentCases){
				String newCaseSource="detectCoveredBranch"+"(" + currCase.getExpression().toSource()  + ", " + "'" + getFunctionName(node.getEnclosingFunction()) + "_" + currCase.getLineno() + "'" +")";
				ExpressionStatement wrappedCondition=(ExpressionStatement)parse(newCaseSource).getFirstChild();
				currCase.setExpression(wrappedCondition.getExpression());
			}
			
			
		}
		
		else if(node instanceof ConditionalExpression){
			
			
			ConditionalExpression conditionalstm=(ConditionalExpression) node;
			AstNode currentCondition=conditionalstm.getTestExpression();
			String newConditonSource="detectCoveredBranch"+"(" + currentCondition.toSource()  + ", " +  "'"  + getFunctionName(node.getEnclosingFunction()) + "_" + conditionalstm.getLineno() + "'" +")";
			ExpressionStatement wrappedCondition=(ExpressionStatement)parse(newConditonSource).getFirstChild();
			conditionalstm.setTestExpression(wrappedCondition.getExpression());
				
			
		}
		return true;
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

	private AstNode createBrnCovgArrayInitialization(FunctionNode func) {
		String funcName=getFunctionName(func);
		String code="initializeBranchCovgArray" + "(" + "'" + funcName + "'" + ");";
		return parse(code);
	}
	
	private AstNode createAdjustBranchCovgAfterFuncCall(FunctionNode caller, FunctionCall callee) {
		String callerName=getFunctionName(caller);
		String calleeName=callee.getTarget().toSource();
		String code="adjustBranchCovgAfterFuncCall" + "(" + "'" + callerName + "'" + ", " + "'" + calleeName + "'" + ");";
		return parse(code);
	}
		

	private AstNode parse(String code) {
		Parser p = new Parser(compilerEnvirons, null);
		return p.parse(code, null, 0);
		
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
