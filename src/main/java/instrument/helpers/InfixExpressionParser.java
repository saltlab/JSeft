package instrument.helpers;

import java.util.ArrayList;
import java.util.Iterator;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.VariableDeclaration;

import instrument.AstInstrumenter;

public class InfixExpressionParser {//extends AstInstrumenter {


	public InfixExpressionParser () {

	}

	public static ArrayList<AstNode> /* ArrayList<AstNode> */ getOperandDependencies(InfixExpression node, boolean addLeft) {

		ArrayList<AstNode> d = new ArrayList<AstNode>();
		ArrayList<AstNode> operands = new ArrayList<AstNode>();
		AstNode operand;
		Iterator<AstNode> it;

		// Un-used right now
		//int operationType = node.getOperator();

		if (addLeft) {
			operands.add(node.getLeft());
		}
		operands.add(node.getRight());

		it = operands.iterator();

		while (it.hasNext()) {
			operand = it.next();
			System.out.println(Token.typeToName(operand.getType()));
			switch (operand.getType()) {
			case org.mozilla.javascript.Token.ADD:  
				// Call recursively (e.g. var a = b + c + d)
				d.addAll(getOperandDependencies((InfixExpression) operand, true));
				break;
			case org.mozilla.javascript.Token.SUB:
				d.addAll(getOperandDependencies((InfixExpression) operand, true));

				break;
			case org.mozilla.javascript.Token.EQ:
				d.addAll(getOperandDependencies((InfixExpression) operand, true));

				break;
			case org.mozilla.javascript.Token.NAME:  
				d.add((Name) operand);
				break;
			case org.mozilla.javascript.Token.THIS:  
				d.add((KeywordLiteral) operand);
				break;
			case org.mozilla.javascript.Token.GETPROP:  
				d.addAll(PropertyGetParser.getPropertyDependencies((PropertyGet) operand));
				break;

			case org.mozilla.javascript.Token.CALL:
				// Add the arguments as dependencies
				d.addAll(FunctionCallParser.getArgumentDependencies((FunctionCall) operand));
				
				// Add the base object as dependency
				FunctionCall operandAsFunctionCall = (FunctionCall) operand;
				if (operandAsFunctionCall.getTarget().getType() == org.mozilla.javascript.Token.GETPROP) {
					// Class method, add class instance as dependency
					d.addAll(PropertyGetParser.getPropertyDependencies((PropertyGet) operandAsFunctionCall.getTarget()));
				}
				
				break;
			case org.mozilla.javascript.Token.NUMBER:  
			case org.mozilla.javascript.Token.STRING:  
				System.out.println("[InfixExpression]: Don't care infix (String or Number)");
				break;
				
			default:
				System.out.println("[InfixExpression]: Error parsing Infix Expression. Unknown operand type. (getNames())");
				System.out.println(Token.typeToName(operand.getType()));
				System.out.println(operand.getClass());
				break;
			}
		}



		return d;
	}

}
