package executionTracer;

import java.io.File;
import java.io.IOException;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;


import astModifier.DOMMuteASTModifier;

import com.crawljax.util.Helper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

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
		String code=null;
		try {
			code=Resources.toString(DOMMutAstInstrumenter.class.getResource("/addDomMut.js"), Charsets.UTF_8);
		} catch (IOException e) {
		
			e.printStackTrace();
		}
/*		File js = new File(this.getClass().getResource("/addDomMut.js").getFile());
		code = Helper.getContent(js);
*/		return parse(code);
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
//		property = property.replaceAll("\\\"", "\\\\\"");
//		property = property.replaceAll("\\\'", "\\\\\'");
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
				code=jsMethodTobeCalled + "(" + "\"" + xpath + "\"" + "," + property  + ")" + ";";  
				break;
			case ".attr":
				jsMethodTobeCalled="changeAttr";
				code=jsMethodTobeCalled + "(" + "\"" + xpath + "\"" + ","  + property + ")" + ";"; 
				break;
			
			case ".prop":
				jsMethodTobeCalled="changeProp";
				code=jsMethodTobeCalled + "(" + "\"" + xpath + "\"" + ","  + property + ")" + ";"; 
				break;
				
			case ".height":
				jsMethodTobeCalled="changeHeight";
				code=jsMethodTobeCalled + "(" + "\"" + xpath + "\"" + ","  + property + ")" + ";"; 
				break;
				
			case ".width":
				jsMethodTobeCalled="changeWidth";
				code=jsMethodTobeCalled + "(" + "\"" + xpath + "\"" + ","  + property + ")" + ";"; 
				break;
				
			case ".addClass":
				jsMethodTobeCalled="changeAddClass";
				code=jsMethodTobeCalled + "(" + "\"" + xpath + "\"" + ","  + property + ")" + ";"; 
				break;
				
			case ".removeClass":
				jsMethodTobeCalled="changeRemoveClass";
				code=jsMethodTobeCalled + "(" + "\"" + xpath + "\"" + ","  + property + ")" + ";"; 
				break;
			
			default:
				break;	
			
			}
		}
		return parse(code);
	
	}



}
