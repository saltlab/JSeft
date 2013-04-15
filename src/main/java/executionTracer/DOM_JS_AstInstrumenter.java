package executionTracer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.Symbol;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import executionTracer.AstInstrumenter.variableUsageType;
import astModifier.DOM_JS_ASTModifier;


public class DOM_JS_AstInstrumenter extends DOM_JS_ASTModifier{
	
	public static final String JSINSTRUMENTLOGNAME = "window.domjsExecutionTrace";

	/**
	 * List with regular expressions of variables that should not be instrumented.
	 */
	private List<String> excludeVariableNamesList = new ArrayList<String>();
	public static enum variableUsageType  {global, inputParam, returnVal};


	/**
	 * Construct without patterns.
	 */
	public DOM_JS_AstInstrumenter() {
		super();
		excludeVariableNamesList = new ArrayList<String>();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public DOM_JS_AstInstrumenter(List<String> excludes) {
		excludeVariableNamesList = excludes;
	}

	/**
	 * Return an AST of the variable logging functions.
	 * 
	 * @return The AstNode which contains functions.
	 */
	private AstNode jsLoggingFunctions() {
		String code=null;
		
		try {
			
			code=Resources.toString(DOMAstInstrumenter.class.getResource("/addVar_domNodePropsAccrossTheXpath.js"), Charsets.UTF_8);
		} catch (IOException e) {
	
			e.printStackTrace();
		}

	/*	File js = new File(this.getClass().getResource("/addVar.js").getFile());
		code = Helper.getContent(js);
	*/	return parse(code);
	}
	
	@Override
	protected AstNode createExitNode(FunctionNode function, ReturnStatement returnNode,String postfix, int lineNo){
		String name;
		String code="";
		String vars = "";
		Set<String> variable=new HashSet<String>();
		TreeSet<String> returnValues=new TreeSet<String>();
		variable=getGlobalVarsInScopeAtExitPoint(function);
/*		VisitObjectTypeVars visitObjectTypeVars=new VisitObjectTypeVars(variableUsageType.returnVal.toString());
		function.visit(visitObjectTypeVars);
		HashSet<String> objectVars=visitObjectTypeVars.getObjectVars();
*/		
		HashSet<String> objectVars=findObjectTypeVarsInScope(function, variableUsageType.returnVal.toString());
		HashSet<String> globVars=findGlobalVarsInScope(function, variableUsageType.returnVal.toString());
		HashSet<String> variables=new HashSet<String>();
		
		variables.addAll(variable);
		variables.addAll(objectVars);
		variables.addAll(globVars);
		
		
		
		if(returnNode!=null){
			AstNode returnVal=returnNode.getReturnValue();
			if(!(returnVal instanceof FunctionNode)){
				returnValues=getReturnValues(returnVal);
			}
		}
		name = getFunctionName(function);

			/* only add instrumentation code if there are variables to log */
			if (variables.size()==0 && returnValues.size()==0) {
				code = "/* empty */";
			} else {
				
				
				code =
			        "send(new Array('" + getScopeName() + "." + name + "', '" + postfix
			                + "', new Array(";
	
				Iterator<String> iter=variables.iterator();
				while(iter.hasNext()){
					String var=iter.next();
					if (shouldInstrument(var)) {
						vars += "addVariable('" + var.split("::")[1] + "', " + var.split("::")[1] + ", " + "'" + var.split("::")[0] + "'" + "),";
					}
				}

			}

			Iterator<String> iter=returnValues.iterator();
			while (iter.hasNext()) {
			/* only instrument variables that should not be excluded */
				String retVal=iter.next();
				if (shouldInstrument(retVal)) {
					vars += "addVariable('" + retVal.split("::")[1] + "', " + retVal.split("::")[1] + ", " + "'" + retVal.split("::")[0] + "'" + "),";
				}
			}
			
		
	
			if (vars.length() > 0) {
			/* remove last comma */
				vars = vars.substring(0, vars.length() - 1);
				code += vars + ")));";
			} else {
			/* no variables to instrument here, so just return an empty node */
				code = "/* empty */";
			}
		
			return parse(code);
		
		
	}

	@Override
	protected AstNode createEnterNode(FunctionNode function, String postfix, int lineNo) {
		String name;
		String code;
		TreeSet<String> variables = new TreeSet<String>();
		variables.addAll(getVariablesNamesInScope(function));
/*		VisitObjectTypeVars visitObjectTypeVars=new VisitObjectTypeVars(variableUsageType.global.toString());
		function.visit(visitObjectTypeVars);
		HashSet<String> objectVars=visitObjectTypeVars.getObjectVars();
*/		
		HashSet<String> objectVars=findObjectTypeVarsInScope(function, variableUsageType.global.toString());
		HashSet<String> globVars=findGlobalVarsInScope(function, variableUsageType.global.toString());
		variables.addAll(objectVars);
		variables.addAll(globVars);

		name = getFunctionName(function);
		

		/* only add instrumentation code if there are variables to log */
		if (variables.size() == 0) {
			code = "/* empty */";
		} else {
			/* TODO: this uses JSON.stringify which only works in Firefox? make browser indep. */
			/* post to the proxy server */
			code =
			        "send(new Array('" + getScopeName() + "." + name + "', '" + postfix
			                + "', new Array(stripScripts(document.getElementsByTagName(\"body\")[0].innerHTML))" + "', new Array(";

			String vars = "";
			Iterator<String> iter=variables.iterator();
			while (iter.hasNext()) {
				String var=iter.next();
				/* only instrument variables that should not be excluded */
				if (shouldInstrument(var)) {
					vars += "addVariable('" + var.split("::")[1] + "', " + var.split("::")[1] + ", " + "'" + var.split("::")[0] + "'" + "),";
				}
			}
			if (vars.length() > 0) {
				/* remove last comma */
				vars = vars.substring(0, vars.length() - 1);
				code += vars + "))));";
			} else {
				/* no variables to instrument here, so just return an empty node */
				code = "/* empty */";
			}
		}
		return parse(code);
	}

	/**
	 * Check if we should instrument this variable by matching it against the exclude variable
	 * regexps.
	 * 
	 * @param name
	 *            Name of the variable.
	 * @return True if we should add instrumentation code.
	 */
	private boolean shouldInstrument(String name) {
		if (name == null) {
			return false;
		}

		/* is this an excluded variable? */
		for (String regex : excludeVariableNamesList) {
			if (name.matches(regex)) {
				LOGGER.debug("Not instrumenting variable " + name);
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns all variables in scope.
	 * 
	 * @param func
	 *            The function.
	 * @return All variables in scope.
	 */
	private TreeSet<String> getVariablesNamesInScope(Scope scope) {
		TreeSet<String> result = new TreeSet<String>();
        Scope origScope=scope;
	

		do {
			/* get the symboltable for the current scope */
			Map<String, Symbol> t = scope.getSymbolTable();
		
			if (t != null) {
				for (String key : t.keySet()) {
					/* read the symbol */
					Symbol symbol = t.get(key);
					/* only add global variables and function parameters */
					if (symbol.getDeclType() == Token.LP)
					{
						result.add(variableUsageType.inputParam + "::" + symbol.getName());
						
						
					}
					else if(symbol.getDeclType()==Token.VAR){
						if(!origScope.equals(scope))	{
							result.add(variableUsageType.global + "::" + symbol.getName());
						}
					}
				}
			}

			/* get next scope (upwards) */
			scope = scope.getEnclosingScope();
		} while (scope != null);

		/* return the result as a String array */
		return result;
	}
	
	private TreeSet<String> getGlobalVarsInScopeAtExitPoint(Scope scope) {
		TreeSet<String> result = new TreeSet<String>();
        Scope origScope=scope;
	

		do {
			/* get the symboltable for the current scope */
			Map<String, Symbol> t = scope.getSymbolTable();
		
			if (t != null) {
				for (String key : t.keySet()) {
					/* read the symbol */
					Symbol symbol = t.get(key);
					//only add global variables
					if(symbol.getDeclType()==Token.VAR){
						if(!origScope.equals(scope))	{
							result.add(variableUsageType.global + "::" + symbol.getName());
						}
					}
				}
			}

			/* get next scope (upwards) */
			scope = scope.getEnclosingScope();
		} while (scope != null);

		/* return the result as a String array */
		return result;
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
						+ ProgramPoint.ENTERPOSTFIX  + "', new Array(AddDomNodeProps("
			            + domNode + ", "
			            + "'" + domNode.replaceAll("\\\'", "\\\\\'") + "'" + ", " + "'" + objectAndFunction + "'" + "))));";
		}
		else{
			code = 
				"send(new Array('" + getScopeName() + "." + funcName + "', '"
					+ ProgramPoint.ENTERPOSTFIX  + "', new Array(AddDomNodeProps("
		            + domNode + ", "
		            + "'" + objectAndFunction.replaceAll("\\\'", "\\\\\'") + "'" + ", " + objectAndFunction.replace("____", " ") + "))));";
		}

	
		return parse(code);
		
	}
	
	private TreeSet<String> getReturnValues(AstNode returnVal){
		TreeSet<String> result = new TreeSet<String>();
		if(returnVal instanceof ObjectLiteral){
			List<ObjectProperty> elements=((ObjectLiteral)returnVal).getElements();
			if(elements!=null){
			
				for(ObjectProperty op:elements){
					if(op.getLeft() instanceof StringLiteral)
						result.add(variableUsageType.returnVal +"::" + ((StringLiteral) op.getLeft()).getValue());
				}
			}
		}
		else if(returnVal!=null)
			result.add(variableUsageType.returnVal +"::" +returnVal.toSource());
		return result;
	}
	
	private HashSet<String> findObjectTypeVarsInScope(Scope scope, String varUsage){
		
		HashSet<String> objectVars=new HashSet<String>();
		VisitObjectTypeVars visitObjectTypeVars=new VisitObjectTypeVars(varUsage, scope);

		while(scope!=null){

			scope.visit(visitObjectTypeVars);
			objectVars.addAll(visitObjectTypeVars.getObjectVars());
			scope=scope.getEnclosingScope();
			
		}
		
		return objectVars;
		
		
	}
	
	private HashSet<String> findGlobalVarsInScope(Scope scope, String varUsage){
		
		HashSet<String> objectVars=new HashSet<String>();
		VisitGlobalVars visitGlobalVars=new VisitGlobalVars(varUsage, scope);

		while(scope!=null){

			scope.visit(visitGlobalVars);
			objectVars.addAll(visitGlobalVars.getObjectVars());
			scope=scope.getEnclosingScope();
			
		}
		
		return objectVars;
		
		
	}
		
	


}
