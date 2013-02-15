package executionTracer;

import java.io.File;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;


import astModifier.DOMMuteASTModifier;

import com.crawljax.util.Helper;

import domMutation.NodeProperty;

public class DOMMutAstInstrumenter extends DOMMuteASTModifier {
	
	public static final String JSINSTRUMENTLOGNAME = "window.jsExecutionTrace";





	/**
	 * Construct without patterns.
	 */
	public DOMMutAstInstrumenter(String funcName, NodeProperty nodeProp, boolean shouldDeleteNode, String stateName) {
		super(funcName, nodeProp, shouldDeleteNode, stateName);
		
	}



	/**
	 * Return an AST of the variable logging functions.
	 * 
	 * @return The AstNode which contains functions.
	 */
	private AstNode jsLoggingFunctions() {
		String code;

		File js = new File(this.getClass().getResource("/addDomMut.js").getFile());
		code = Helper.getContent(js);
		return parse(code);
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
	protected AstNode createMutationNode(FunctionNode function, String xpath,
			String postfix, String accessType, String property, boolean shouldDeleteNode) {
		property = property.replaceAll("\\\"", "\\\\\"");
		property = property.replaceAll("\\\'", "\\\\\'");
		xpath=xpath.replaceAll("\\\"", "\\\\\"");
		xpath=xpath.replaceAll("\\\'", "\\\\\'");
		
	
		String code="";
		String jsMethodTobeCalled="";
		if(shouldDeleteNode){
		
			code= "deleteElement" + "(" + "\"" + xpath +  "\"" + ")" + ";"; 
		}
		
		else{
			switch(accessType){
			case ".css":
				jsMethodTobeCalled="changeCssAttr";
				code=jsMethodTobeCalled + "(" + "\"" + xpath + "\"" + "," + "\""+ property + "\"" + ")" + ";";  
				break;
			case ".attr":
				jsMethodTobeCalled="changeAttr";
				code=jsMethodTobeCalled + "(" + "\"" + xpath + "\"" + "," + "\"" + property + "\"" + ")" + ";"; 
				break;
			
			default:
				break;	
			
			}
		}
		return parse(code);
	
	}



}
