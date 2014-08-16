package instrument;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.Symbol;
import instrument.helpers.FunctionCallParser;
import instrument.helpers.InfixExpressionParser;
import instrument.helpers.ObjectLiteralParser;
import instrument.helpers.PropertyGetParser;

public class FunctionCallerDependencies extends AstInstrumenter {

	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
	private ErrorReporter errorReporter = compilerEnvirons.getErrorReporter();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;

	/**
	 * List with regular expressions of variables that should not be instrumented.
	 */
	private ArrayList<String> excludeList = new ArrayList<String>();

	private ArrayList<AstNode> dataDependencies = new ArrayList<AstNode>();
	private ArrayList<Name> controlDependencies = new ArrayList<Name>();

	/**
	 * Construct without patterns.
	 */
	public FunctionCallerDependencies() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public FunctionCallerDependencies(ArrayList<String> excludes) {
		super(excludes);
		excludeList = excludes;
	}

	/**
	 * Parse some JavaScript to a simple AST.
	 * 
	 * @param code
	 *            The JavaScript source code to parse.
	 * @return The AST node.
	 */
	public AstRoot parse(String code, int lineno) {
		Parser p = new Parser(compilerEnvirons, errorReporter);

		//System.out.println(code);
		return p.parse(code, null, lineno);
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
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
	}

	/**
	 * @param scopeName
	 *            the scopeName to set
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	/**
	 * @return the scopeName
	 */
	public String getScopeName() {
		return scopeName;
	}

	public ArrayList<AstNode> getDataDependencies () {
		return this.dataDependencies;
	}

	public void clearDataDependencies () {
		this.dataDependencies = new ArrayList<AstNode>();
	}

	private static Scope topMost;

	public void setTopScope(Scope s) {
		this.topMost = s;
	}

	public Scope getTopScope() {
		return topMost;
	}

	@Override
	public  boolean visit(AstNode node){
		int tt = node.getType();
		int targetType;
		AstNode target;
		String targetFunction;

		if (tt == org.mozilla.javascript.Token.CALL) {
			// TODO:   uncomment this vv
			target = ((FunctionCall) node).getTarget();
			targetType = target.getType();
			targetFunction = node.toSource();

			if (targetType == org.mozilla.javascript.Token.NAME) {
				// Regular function call, 39
				// E.g. parseInt, print, startClock
				targetFunction = target.toSource();
			} else if (targetType == org.mozilla.javascript.Token.GETPROP) {
				// Class specific function call, 33
				// E.g. document.getElementById, e.stopPropagation
				String[] methods = targetFunction.split("\\.");
				targetFunction = methods[methods.length-1];
			} 

			if (targetFunction.equals(functionName)) {
				handleFunctionCall((FunctionCall) node);
			}
		} 
		return true;  // process kids
	}

	private void handleFunctionCall(FunctionCall node) {
		List<AstNode> args = node.getArguments();
		Iterator<AstNode> it = args.iterator();
		AstNode currentArgument;
		int argumentType;
		int i = 0;

		while (it.hasNext()) {
			currentArgument = it.next();

			if (i == argumentNumber) {
				// This is the argument we are interested in

				argumentType = currentArgument.getType();

				if (argumentType == org.mozilla.javascript.Token.FUNCTION) {
				} else if (argumentType == org.mozilla.javascript.Token.CALL) {
					// Must slice return statement of function (could be defined elsewhere)
					// Must add arguments 

					dataDependencies.addAll(FunctionCallParser.getArgumentDependencies((FunctionCall) currentArgument));
				} else if (argumentType == org.mozilla.javascript.Token.NAME) {
					// Add name to data dependencies (variableName, lineNo)

					dataDependencies.add((Name) currentArgument);
				} else if (argumentType == org.mozilla.javascript.Token.GETPROP) {
					// Need to check if there is over lap with CALL (method calls, which do they fall under)

					dataDependencies.addAll(PropertyGetParser.getPropertyDependencies((PropertyGet) currentArgument));
				} else if (argumentType == org.mozilla.javascript.Token.ADD) {
					// Investigate how to get all variables in the add/concatination

					InfixExpression addOperation = ((InfixExpression) currentArgument);
					dataDependencies.addAll(InfixExpressionParser.getOperandDependencies((InfixExpression) currentArgument, true));
				} else if (argumentType == org.mozilla.javascript.Token.SUB) {

					InfixExpression subOperation = ((InfixExpression) currentArgument);
					dataDependencies.addAll(InfixExpressionParser.getOperandDependencies((InfixExpression) currentArgument, true));
					// Investigate how to get all variables in the sub
				} else if (argumentType == org.mozilla.javascript.Token.NEG) {

					// Investigate how to get all variables in the sub
				} else if (argumentType == org.mozilla.javascript.Token.POS) {

					// Investigate how to get all variables in the sub
				} else if (argumentType == org.mozilla.javascript.Token.OBJECTLIT) {
					dataDependencies.addAll(ObjectLiteralParser.getArgumentDependencies((ObjectLiteral) currentArgument));
				} else if (argumentType == org.mozilla.javascript.Token.STRING
						|| argumentType == org.mozilla.javascript.Token.NUMBER
						|| argumentType == org.mozilla.javascript.Token.NULL
						|| argumentType == org.mozilla.javascript.Token.FALSE
						|| argumentType == org.mozilla.javascript.Token.TRUE) {
					// Don't care, no new dependencies
				} else if (argumentType == org.mozilla.javascript.Token.THIS) {
					Name thisName = new Name();
					thisName.setScope(node.getEnclosingScope());
					thisName.setLineno(currentArgument.getLineno());
					thisName.setIdentifier("this");
					dataDependencies.add(thisName);
				}else {
					System.out.println("[FunctionCallerDependencies - handleFunctionCall]: Unknown right hand side type. " + Token.typeToName(argumentType));
				}
			}

			i++;
		}

	}

	@Override
	public AstNode createNodeInFunction(FunctionNode function, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AstNode createNode(FunctionNode function, String postfix, int lineNo) {
		String name;
		String code;

		name = getFunctionName(function);
		if (postfix == ":::EXIT") {
			postfix += lineNo;
		}

		/* only add instrumentation code if there are variables to log */

		/* TODO: this uses JSON.stringify which only works in Firefox? make browser indep. */
		/* post to the proxy server */
		code = "send(new Array('" + getScopeName() + "." + name + "', '" + postfix + "'));";

		return parse(code, lineNo);
	}

	@Override
	public AstNode createPointNode(String shouldLog, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AstRoot finish(AstRoot node) {
		// Return new instrumented node/code
		return node;
	}

	@Override
	public void start(String node) {
	}

	public void start() {
	}

	/**
	 * Returns all variables in scope.
	 * 
	 * @param func
	 *            The function.
	 * @return All variables in scope.
	 */
	protected String[] getVariablesNamesInScope(Scope scope) {
		TreeSet<String> result = new TreeSet<String>();

		do {
			/* get the symboltable for the current scope */
			Map<String, Symbol> t = scope.getSymbolTable();

			if (t != null) {
				for (String key : t.keySet()) {
					/* read the symbol */
					Symbol symbol = t.get(key);
					/* only add variables and function parameters */
					if (symbol.getDeclType() == Token.LP || symbol.getDeclType() == Token.VAR) {
						result.add(symbol.getName());
					}
				}
			}

			/* get next scope (upwards) */
			scope = scope.getEnclosingScope();
		} while (scope != null);

		/* return the result as a String array */
		return result.toArray(new String[0]);
	}

	protected String[] getVariablesNamesInFunction(Scope scope) {
		TreeSet<String> result = new TreeSet<String>();

		/* get the symboltable for the current scope */
		Map<String, Symbol> t = scope.getSymbolTable();

		if (t != null) {
			for (String key : t.keySet()) {
				/* read the symbol */
				Symbol symbol = t.get(key);
				/* only add variables and function parameters */
				if (symbol.getDeclType() == Token.LP || symbol.getDeclType() == Token.VAR) {
					result.add(symbol.getName());
				}
			}
		}

		/* return the result as a String array */
		return result.toArray(new String[0]);
	}

	/**
	 * Check if we should instrument this variable by matching it against the exclude variable
	 * regexps.
	 * 
	 * @param name
	 *            Name of the variable.
	 * @return True if we should add instrumentation code.
	 */
	protected boolean shouldInstrument(String name) {
		if (name == null) {
			return false;
		}

		/* is this an excluded variable? */
		for (String regex : excludeList) {
			if (name.matches(regex)) {
				return false;
			}
		}
		return true;
	}

	public ArrayList<String> getExcludeList() {
		return this.excludeList;
	}

	private String getFunctionNodeName(FunctionNode node){
		AstNode parent = node.getParent();
		String name = node.getName();

		if (name == "" && parent.getType() == org.mozilla.javascript.Token.ASSIGN) {
			name = parent.toSource().substring(0,parent.toSource().indexOf(node.toSource()));
			name = name.substring(name.lastIndexOf(".")+1,name.indexOf("="));
		}
		return name;
	}

	static private ArrayList<AstNode> dependencies = new ArrayList<AstNode>();

	public ArrayList<AstNode> getNextSliceStart() {
		return dependencies;
	}

	private String functionName = null;

	public void setFunctionName(String name) {
		this.functionName = name;
	}

	public String getFunctionName () {
		return this.functionName;
	}

	private int argumentNumber = -1;

	public void setArgumentNumber(int n) {
		this.argumentNumber = n;
	}

	public int getArgumentNumber () {
		return this.argumentNumber;
	}

}