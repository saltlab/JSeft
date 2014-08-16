package instrument.helpers;

import java.util.ArrayList;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.UnaryExpression;

public class NotParser {
	public NotParser () {

	}

	public static ArrayList<AstNode> getNotDependencies(UnaryExpression r) {

		ArrayList<AstNode> p = new ArrayList<AstNode>();
		AstNode operand = r.getOperand();

		switch (operand.getType()) {
		case org.mozilla.javascript.Token.NAME:  
			p.add((Name) operand);
			break;
		case org.mozilla.javascript.Token.THIS:  
			p.add((KeywordLiteral) operand);
			break;
		case org.mozilla.javascript.Token.GETPROP:
			p.addAll(PropertyGetParser.getPropertyDependencies((PropertyGet) operand));			
			break;
		case org.mozilla.javascript.Token.CALL:  
			p.addAll(FunctionCallParser.getArgumentDependencies((FunctionCall) operand));
			break;
		default:
			System.out.println("[NotParser]: Error parsing Unary Expression. Unknown operand type.");
			break;

		}

		return p;
	}
}
