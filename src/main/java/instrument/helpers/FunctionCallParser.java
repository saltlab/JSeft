package instrument.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;

public class FunctionCallParser {

	public FunctionCallParser () {

	}

	public static ArrayList<AstNode> /* ArrayList<AstNode> */ getArgumentDependencies(FunctionCall node) {
		ArrayList<AstNode> a = new ArrayList<AstNode>();

		// Iterate through arguments
		List<AstNode> args = node.getArguments();
		Iterator<AstNode> it = args.iterator();
		AstNode nextArg;

		while (it.hasNext()) {
			nextArg = it.next();

			switch (nextArg.getType()) {
			case org.mozilla.javascript.Token.ADD:  

				a.addAll(InfixExpressionParser.getOperandDependencies((InfixExpression) nextArg, true));
				break;
			case org.mozilla.javascript.Token.SUB:

				a.addAll(InfixExpressionParser.getOperandDependencies((InfixExpression) nextArg, true));
				break;
			case org.mozilla.javascript.Token.CALL:  

				a.addAll(getArgumentDependencies((FunctionCall) nextArg));
				break;
			case org.mozilla.javascript.Token.NAME:  
				a.add((Name) nextArg);
				break;
			case org.mozilla.javascript.Token.THIS:  
				a.add((KeywordLiteral) nextArg);
				break;
			case org.mozilla.javascript.Token.STRING:  
			case org.mozilla.javascript.Token.NUMBER:  
			case org.mozilla.javascript.Token.FUNCTION: 
				// Function might been to be added to dependencies
				break;
			case org.mozilla.javascript.Token.GETPROP:  
				
				a.addAll(PropertyGetParser.getPropertyDependencies((PropertyGet) nextArg));
				
			default:
				System.out.println("[FunctionCallParser]: Error parsing function call Expression. Unknown operand type.");
				break;
			}
		}

		return a;

	}
}
