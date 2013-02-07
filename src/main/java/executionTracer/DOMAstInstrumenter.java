package executionTracer;

import java.io.File;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.ReturnStatement;
import com.crawljax.util.Helper;
import astModifier.DOMASTModifier;

public class DOMAstInstrumenter extends DOMASTModifier {
	public static final String JSINSTRUMENTLOGNAME = "window.jsExecutionTrace";





	/**
	 * Construct without patterns.
	 */
	public DOMAstInstrumenter() {
		super();
		
	}



	/**
	 * Return an AST of the variable logging functions.
	 * 
	 * @return The AstNode which contains functions.
	 */
	private AstNode jsLoggingFunctions() {
		String code;

		File js = new File(this.getClass().getResource("/addvariable.js").getFile());
		code = Helper.getContent(js);
		return parse(code);
	}
	
	@Override
	protected AstNode createExitNode(FunctionNode function, ReturnStatement returnNode,String postfix, int lineNo){
		
	return null;
		
		
	}

	@Override
	protected AstNode createEnterNode(FunctionNode function, String postfix, int lineNo) {
		return null;
	}



	
	
	


	@Override
	public void finish(AstRoot node) {
		/* add initialization code for the function and logging array */
		node.addChildToFront(jsLoggingFunctions());
	}

	@Override
	public void start() {
		/* nothing to do here */
	}

	@Override
	protected AstNode createPointNode(FunctionNode func, String domNode, String objectAndFunction, int lineNo) {

		String funcName=getFunctionName(func);
		String code = 
			"send(new Array('" + getScopeName() + "." + funcName + "', '"
            + ProgramPoint.EXITPOSTFIX  + "', new Array(addVariable("
            + domNode + ", "
            + objectAndFunction.replace("____", " ") + "))));";

	
		return parse(code);
		
	}
	
	
		
	

}
