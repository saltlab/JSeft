package astModifier;

import java.util.List;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.WhileLoop;

public class BranchCvgCalc implements NodeVisitor {
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	@Override
	public boolean visit(AstNode node) {
		
		if(node instanceof FunctionNode){
			FunctionNode func=(FunctionNode)node;
			AstNode covgArray=createBrnCovgArrayInitialization();

			func.getBody().addChildToFront(covgArray);
		}
		
		if(node instanceof IfStatement){
			IfStatement ifstm=(IfStatement) node;
			AstNode currentCondition=ifstm.getCondition();
			String newConditonSource="detectCoveredBranch"+"(" + currentCondition.toSource() + ", " + "'" + ifstm.getLineno() + "'" +")";
			AstNode wrappedCondition=parse(newConditonSource);
			ifstm.setCondition(wrappedCondition);
		}
		
		else if(node instanceof WhileLoop){
			
				
			WhileLoop whilestm=(WhileLoop) node;
			AstNode currentCondition=whilestm.getCondition();
			String newConditonSource="detectCoveredBranch"+"("  + currentCondition.toSource() + ", " + "'" + whilestm.getLineno() + "'" +")";
			AstNode wrappedCondition=parse(newConditonSource);
			whilestm.setCondition(wrappedCondition);
				
			
		}
		
		else if(node instanceof ForLoop){
			
			
			ForLoop forstm=(ForLoop) node;
			AstNode currentCondition=forstm.getCondition();
			String newConditonSource="detectCoveredBranch"+"(" + currentCondition.toSource() +  ", " + "'"  + forstm.getLineno() + "'"  +")";
			AstNode wrappedCondition=parse(newConditonSource);
			forstm.setCondition(wrappedCondition);
				
			
		}
		
		else if(node instanceof ForLoop){
			
			
			ForLoop forstm=(ForLoop) node;
			AstNode currentCondition=forstm.getCondition();
			String newConditonSource="detectCoveredBranch"+"(" + currentCondition.toSource() + ", " + "'" + forstm.getLineno() +"'" +")";
			AstNode wrappedCondition=parse(newConditonSource);
			forstm.setCondition(wrappedCondition);
				
			
		}
		
		else if(node instanceof SwitchStatement){
			
			
			SwitchStatement switchstm=(SwitchStatement) node;
			List<SwitchCase> currentCases=switchstm.getCases();
			for(SwitchCase currCase:currentCases){
				String newCaseSource="detectCoveredBranch"+"(" + currCase.getExpression().toSource()  + ", " + "'" + currCase.getLineno() + "'" +")";
				AstNode wrappedCondition=parse(newCaseSource);
				currCase.setExpression(wrappedCondition);
			}
			
			
		}
		
		else if(node instanceof ConditionalExpression){
			
			
			ConditionalExpression conditionalstm=(ConditionalExpression) node;
			AstNode currentCondition=conditionalstm.getTestExpression();
			String newConditonSource="detectCoveredBranch"+"(" + currentCondition.toSource()  + ", " +  "'"  + conditionalstm.getLineno() + "'" +")";
			AstNode wrappedCondition=parse(newConditonSource);
			conditionalstm.setTestExpression(wrappedCondition);
				
			
		}
		return true;
	}

	private AstNode createBrnCovgArrayInitialization() {
		String code="initializeBranchCovgArray();";
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
