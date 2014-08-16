package instrument;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.VariableDeclaration;

public class SimpleSearcher extends AstInstrumenter {
	
	private static boolean elementFound = false;
	private static String variableName;

	@Override
	public AstNode createNodeInFunction(FunctionNode function, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AstNode createNode(FunctionNode function, String postfix, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start(String node) {
		// TODO Auto-generated method stub

	}

	@Override
	public AstNode createPointNode(String objectAndFunction, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean visit(AstNode node) {
		int tt = node.getType();

		if (tt == org.mozilla.javascript.Token.NAME && ((Name) node).getIdentifier().equals(variableName)) {
			// Might need stricter check since target variable could appear multiple times on single line

			elementFound = true;
		}
		return true;
	}
	
	public boolean checkIfChild (Name node) {
		elementFound = false;
		variableName = node.getIdentifier();

		this.visit(node);
		
		return elementFound;
	}
	
	public boolean getFound () {
		return elementFound;
	}
	
	public void setVariableName (String target) {
		this.variableName = target;
	}

}
