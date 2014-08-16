package instrument;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import units.FunctionArgumentPair;

public class FunctionDeclarationFinder extends AstInstrumenter {

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

		if (tt == org.mozilla.javascript.Token.FUNCTION && ((FunctionNode) node).getName().equals(this.fap.getFunctionName())) {
			handleFunctionDeclaration((FunctionNode) node);
		}

		return true;
	}

	private void handleFunctionDeclaration(FunctionNode node) {
		
		List<AstNode> args = node.getParams();
		
		for (int j = 0; j < fap.getArgumentsOfInterest().size(); j++) {
			if (fap.getArgumentsOfInterest().get(j) >= args.size()) {
				continue;
			}
			
			System.out.println("=-=-=-=-=-=--=-=-=-=-=-");
			System.out.println(node.getName());
			System.out.println(args.get(fap.getArgumentsOfInterest().get(j)).toSource());
			System.out.println("=-=-=-=-=-=--=-=-=-=-=-");
			
			// Want to track this node
			if (argumentNodes.indexOf(args.get(fap.getArgumentsOfInterest().get(j))) == -1) {
				argumentNodes.add(args.get(fap.getArgumentsOfInterest().get(j)));
			}
		}
		
	}

	private FunctionArgumentPair fap;

	public void setFunctionArgumentPair (FunctionArgumentPair fa) {
		this.fap = fa;
		this.argumentNodes = new ArrayList<AstNode>();
	}

	private ArrayList<AstNode> argumentNodes = new ArrayList<AstNode>();

	public ArrayList<AstNode> getArgumentsNode () {
		return this.argumentNodes;
	}

}
