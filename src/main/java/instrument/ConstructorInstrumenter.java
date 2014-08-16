package instrument;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;

public class ConstructorInstrumenter extends AstInstrumenter {
	// Responsible for tracking when "complex" objects are created
	// Adds a public property to each custom class for tracking of class instances

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
		// TODO Auto-generated method stub
		return false;
	}
	

}
