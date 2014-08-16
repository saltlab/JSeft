package instrument.helpers;

import java.util.ArrayList;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;

public class ReturnParser {

	public ReturnParser () {

	}

	public static ArrayList<AstNode> getReturnValueDependencies(ReturnStatement r) {

		ArrayList<AstNode> p = new ArrayList<AstNode>();
		AstNode returnValue = r.getReturnValue();

		switch (returnValue.getType()) {
		case org.mozilla.javascript.Token.NAME:  
			p.add((Name) returnValue);
			break;
		case org.mozilla.javascript.Token.THIS:  
			p.add((KeywordLiteral) returnValue);
			break;
		case org.mozilla.javascript.Token.GETPROP:
			p.addAll(PropertyGetParser.getPropertyDependencies((PropertyGet) returnValue));			
			break;
		case org.mozilla.javascript.Token.CALL:  
			p.addAll(FunctionCallParser.getArgumentDependencies((FunctionCall) returnValue));
			break;
		case org.mozilla.javascript.Token.ADD:
			p.addAll(InfixExpressionParser.getOperandDependencies((InfixExpression) returnValue, true));
			break;
		case org.mozilla.javascript.Token.SUB:
			InfixExpressionParser.getOperandDependencies((InfixExpression) returnValue, true);
			break;
		default:
			System.out.println("[InfixExpression]: Error parsing Infix Expression. Unknown operand type. (getNames())");
			break;

		}

		return p;
	}

}
