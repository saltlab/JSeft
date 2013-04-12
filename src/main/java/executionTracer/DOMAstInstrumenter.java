package executionTracer;

import java.io.File;
import java.io.IOException;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ReturnStatement;
import com.crawljax.util.Helper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import astModifier.DOMASTModifier;

public class DOMAstInstrumenter extends DOMASTModifier {
	public static final String JSINSTRUMENTLOGNAME = "window.domExecutionTrace";





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
		String code=null;
		try {
			code=Resources.toString(DOMAstInstrumenter.class.getResource("/domNodeProps.js"), Charsets.UTF_8);
		} catch (IOException e) {
	
			e.printStackTrace();
		}
/*		File js = new File(this.getClass().getResource("/domNodeProps.js").getFile());
		code = Helper.getContent(js);
*/		return parse(code);
	}
	
	@Override
	protected AstNode createExitNode(FunctionNode function, ReturnStatement returnNode,String postfix, int lineNo){
		
	return null;
		
		
	}

	@Override
	protected AstNode createEnterNode(FunctionNode function, String postfix, int lineNo) {
		return null;
	}

	/**
	 * Find out the function name of a certain node and return "anonymous" if it's an anonymous
	 * function.
	 * 
	 * @param f
	 *            The function node.
	 * @return The function name.
	 */
	protected String getFunctionName(FunctionNode f) {
		if (f==null)
			return "NoFunctionNode";
	/*	else if(f.getParent() instanceof LabeledStatement){
			return ((LabeledStatement)f.getParent()).shortName();
		}
	*/	else if(f.getParent() instanceof ObjectProperty){
			return ((ObjectProperty)f.getParent()).getLeft().toSource();
		}
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
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
		String code =null;
		if(objectAndFunction.equals("DIRECTACCESS")){
			code = 
					"send(new Array('" + getScopeName() + "." + funcName + "', '"
						+ ProgramPoint.POINTPOSTFIX  + "', new Array(AddDomNodeProps("
			            + domNode + ", "
			            + "'" + domNode.replaceAll("\\\'", "\\\\\'") + "'" + ", " + "'" + objectAndFunction + "'" + "))));";
		}
		else{
			code = 
				"send(new Array('" + getScopeName() + "." + funcName + "', '"
					+ ProgramPoint.POINTPOSTFIX  + "', new Array(AddDomNodeProps("
		            + domNode + ", "
		            + "'" + objectAndFunction.replaceAll("\\\'", "\\\\\'") + "'" + ", " + objectAndFunction.replace("____", " ") + "))));";
		}

	
		return parse(code);
		
	}
	
	
		
	

}
