package instrument.helpers;

import java.util.ArrayList;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;


public class PropertyGetParser {

	public PropertyGetParser () {

	}

	public static ArrayList<AstNode> getPropertyDependencies(PropertyGet rightSide) {
		
		ArrayList<AstNode> p = new ArrayList<AstNode>();

		AstNode object = rightSide.getTarget();
		
		// Not used currently, naive case
		AstNode property = rightSide.getProperty();

		switch (object.getType()) {
		case org.mozilla.javascript.Token.NAME:  
			p.add((Name) object);
			break;
		case org.mozilla.javascript.Token.GETPROP:
			p.addAll(getPropertyDependencies((PropertyGet) object));			
			break;
		case org.mozilla.javascript.Token.CALL:  
			p.addAll(FunctionCallParser.getArgumentDependencies((FunctionCall) object));
			break;
		case org.mozilla.javascript.Token.THIS:  
			p.add((KeywordLiteral) object);
			break;
		default:
			System.out.println("[InfixExpression]: Error parsing Infix Expression. Unknown operand type. (getNames())");
			break;

		}
		
		return p;
	}

}
