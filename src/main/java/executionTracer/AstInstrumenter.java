package executionTracer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import com.crawljax.plugins.aji.executiontracer.ProgramPoint;
import com.crawljax.util.Helper;

import astModifier.JSASTModifier;

public class AstInstrumenter extends JSASTModifier{

	public static final String JSINSTRUMENTLOGNAME = "window.jsExecutionTrace";

	/**
	 * List with regular expressions of variables that should not be instrumented.
	 */
	private List<String> excludeVariableNamesList = new ArrayList<String>();



	/**
	 * Construct without patterns.
	 */
	public AstInstrumenter() {
		super();
		excludeVariableNamesList = new ArrayList<String>();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public AstInstrumenter(List<String> excludes) {
		excludeVariableNamesList = excludes;
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
		String name;
		String code;
		AstNode returnVal=returnNode.getReturnValue();
		String[] retuenValues=getReturnValues(returnVal);
		name = getFunctionName(function);
		

		/* only add instrumentation code if there are variables to log */
		if (returnVal instanceof FunctionNode) {
			code = "/* empty */";
		} else {
			
			
			code =
		        "send(new Array('" + getScopeName() + "." + name + "', '" + postfix
		                + "', new Array(";

			String vars = "";
			for (int i = 0; i < retuenValues.length; i++) {
				/* only instrument variables that should not be excluded */
				if (shouldInstrument(retuenValues[i])) {
					vars += "addVariable('" + retuenValues[i] + "', " + retuenValues[i] + "),";
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
		}
	return parse(code);
		
		
	}

	@Override
	protected AstNode createEnterNode(FunctionNode function, String postfix, int lineNo) {
		String name;
		String code;
		String[] variables = getVariablesNamesInScope(function);

		name = getFunctionName(function);
		

		/* only add instrumentation code if there are variables to log */
		if (variables.length == 0) {
			code = "/* empty */";
		} else {
			/* TODO: this uses JSON.stringify which only works in Firefox? make browser indep. */
			/* post to the proxy server */
			code =
			        "send(new Array('" + getScopeName() + "." + name + "', '" + postfix
			                + "', new Array(";

			String vars = "";
			for (int i = 0; i < variables.length; i++) {
				/* only instrument variables that should not be excluded */
				if (shouldInstrument(variables[i])) {
					vars += "addVariable('" + variables[i] + "', " + variables[i] + "),";
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
	private String[] getVariablesNamesInScope(Scope scope) {
		TreeSet<String> result = new TreeSet<String>();
        Scope origScope=scope;
	

		do {
			/* get the symboltable for the current scope */
			Map<String, Symbol> t = scope.getSymbolTable();
		
			if (t != null) {
				for (String key : t.keySet()) {
					/* read the symbol */
					Symbol symbol = t.get(key);
					/* only add variables and function parameters */
					if (symbol.getDeclType() == Token.LP)
					{
						result.add(symbol.getName());
						
						
					}
					else if(symbol.getDeclType()==Token.VAR){
						if(!origScope.equals(scope))	{
							result.add(symbol.getName());
						}
					}
				}
			}

			/* get next scope (upwards) */
			scope = scope.getEnclosingScope();
		} while (scope != null);

		/* return the result as a String array */
		return result.toArray(new String[0]);
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
	protected AstNode createPointNode(String objectAndFunction, int lineNo) {

		String code =
		        "send(new Array('" + getScopeName() + "line" + lineNo + "', '"
		                + ProgramPoint.POINTPOSTFIX + lineNo + "', new Array(addVariable('"
		                + objectAndFunction.replaceAll("\\\'", "\\\\\'") + "', "
		                + objectAndFunction.replace("____", " ") + "))));";

	
		return parse(code);
		
	}
	
	private String[] getReturnValues(AstNode returnVal){
		TreeSet<String> result = new TreeSet<String>();
		if(returnVal instanceof ObjectLiteral){
			List<ObjectProperty> elements=((ObjectLiteral)returnVal).getElements();
			if(elements!=null){
			
				for(ObjectProperty op:elements){
					if(op.getLeft() instanceof StringLiteral)
						result.add(((StringLiteral) op.getLeft()).getValue());
				}
			}
		}
		else if(returnVal!=null)
			result.add(returnVal.toSource());
		return result.toArray(new String[0]);
	}
		
	


}
