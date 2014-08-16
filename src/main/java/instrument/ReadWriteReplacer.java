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
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.CatchClause;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.Symbol;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;


import configuration.TraceHelper;
import instrument.helpers.FunctionCallParser;
import units.SlicingCriteria;

public class ReadWriteReplacer extends AstInstrumenter {

	private static final String TOOLNAME = "_dyno";
	private static final String VARREAD = "_dynoRead";
	private static final String ARGREAD = "_dynoReadAsArg";
	private static final String VARWRITE = "_dynoWrite";
	private static final String VARWRITEAUG = "_dynoWriteAug";
	private static final String VARWRITEPROP = "_dynoWriteProp";
	private static final String VARWRITEFUNCRET = "_dynoWriteReturnValue";
	private static final String PROPREAD = "_dynoReadProp";
	private static final String FUNCCALL = "_dynoFunc";

	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
	private ErrorReporter errorReporter = compilerEnvirons.getErrorReporter();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;
	private int lineNo = -1;

	/**
	 * List with regular expressions of variables that should not be instrumented.
	 */
	private ArrayList<String> excludeList = new ArrayList<String>();


	/**
	 * Construct without patterns.
	 */
	public ReadWriteReplacer() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public ReadWriteReplacer(ArrayList<String> excludes) {
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
	public AstRoot parse(String code, int lineNo) {
		compilerEnvirons.setRecordingLocalJsDocComments(true);
		compilerEnvirons.setAllowSharpComments(true);
		compilerEnvirons.setRecordingComments(true);
		compilerEnvirons.setOptimizationLevel(-1);
		Parser p = new Parser(compilerEnvirons, errorReporter);

		System.out.println("[parsing compilerEnvirons]: ");



		code = code.replaceAll("\\;\\\n\\ \\,", ",")
				//.replaceAll("\"", "\'")
				.replaceAll("\\.\\[", "[")
				.replaceAll("\\;\\\n\\)", ")");

		AstRoot returnMe = null;
		try {
			returnMe = p.parse(code, null, lineNo);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(code);
		}

		return returnMe;
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




	private static Scope topMost;

	public void setTopScope(Scope s) {
		this.topMost = s;
	}

	public void setLineNo(int l) {
		this.lineNo = l;
	}

	public Scope getTopScope() {
		return topMost;
	}

	@Override
	public  boolean visit(AstNode node){
		int tt = node.getType();

		if (tt == org.mozilla.javascript.Token.GETPROP) {
			// TODO:
			handleProperty((PropertyGet) node);
		} else if (tt == org.mozilla.javascript.Token.VAR && node instanceof VariableDeclaration
				// Weird glitch/bug in Mozilla Rhino (variable declarations appear twice when crawling AST)
				&& node.getParent().getType() != org.mozilla.javascript.Token.VAR ) {
			// TODO:
			handleVariableDeclaration((VariableDeclaration) node);
		} else if (tt == org.mozilla.javascript.Token.VAR && !(node instanceof VariableDeclaration)) {
			System.out.println("interesting case");
			System.out.println(node.getClass().toString());
			System.out.println(node.toSource());
		} else if (tt == org.mozilla.javascript.Token.ASSIGN
				|| tt == org.mozilla.javascript.Token.ASSIGN_ADD
				|| tt == org.mozilla.javascript.Token.ASSIGN_SUB) {


			handleAssignmentOperator((Assignment) node);
		} else if (tt == org.mozilla.javascript.Token.CALL
				&& !((FunctionCall) node).getTarget().toSource().contains(TOOLNAME)) {
			// TODO:


			handleFunctionCall((FunctionCall) node);
		} else if (tt == org.mozilla.javascript.Token.CALL) {

			System.out.println(((FunctionCall) node).getTarget().toSource());
			System.out.println(((FunctionCall) node).getTarget().toSource().contains(ARGREAD));
		} else if (tt == org.mozilla.javascript.Token.NAME
				&& isItInteresting(((Name) node).getIdentifier(), node.getLineno()) /*node.getLineno() == lineNo && ((Name) node).getIdentifier().equals(variableName)*/) {
			// TODO:

			// Might need stricter check since target variable could appear multiple times on single line
			handleName((Name) node);
		} else if (tt == org.mozilla.javascript.Token.GETELEM){
			// TODO: GETELEM   class org.mozilla.javascript.ast.ElementGet

			handleGetElem((ElementGet) node);
		} else if (tt == org.mozilla.javascript.Token.CATCH) {
			handleCatch((CatchClause) node);
		} else if (tt == org.mozilla.javascript.Token.INC
				|| tt == org.mozilla.javascript.Token.DEC) {
			handleUnaryExpression((UnaryExpression) node);
			return false;
		}

		return true;  // process kids
	}

	private static void handleCatch (CatchClause node) {
		System.out.println(node.toSource());
		System.out.println(node.getBody().toSource());
		//    	System.out.println(node.getCatchCondition().toSource());
		System.out.println(node.getVarName().toSource());
	}

	@Override
	public AstNode createNodeInFunction(FunctionNode function, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean targetIsChild(Name node) {
		SimpleSearcher ss = new SimpleSearcher();

		return ss.checkIfChild(node);
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
		// Adds necessary instrumentation to the root node src
		String isc = node.toSource().replaceAll("\\)]\\;+\\n+\\(", ")](")
				.replaceAll("\\)\\;\\n+\\(", ")(")
				.replaceAll("\\;\\n+\\;", ";")
				.replaceAll("\\;\\n+\\.", ".")
				.replaceAll("\\;\\n+\\,", ",")
				.replaceAll("\\;\\n+\\)", ")")
				//.replaceAll("\\ \\.", " ")
				.replaceAll("(\\n\\;\\n)", "\n\n")
				.replaceAll("\\;\\n\\+\\+", "++")
				.replaceAll("\\;\\n\\-\\-", "--")
				.replaceAll("\\)\\;\\n+\\)\\;", "));")
				.replaceAll("\\\\t", "    ")
				//	.replaceAll("(\\n)", "\n\n")  // <-- just for spacing, might not be needed
				.replaceAll("\\.\\[", "[");

		AstRoot iscNode = null;
		try {
			iscNode  = rhinoCreateNode(isc);
		} catch (Exception e) {
			System.out.println(isc);
		}

		// Return new instrumented node/code
		return iscNode;
	}

	@Override
	public void start(String node) {
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

	private ArrayList<String> getClosure(AstNode node){
		ArrayList<String> parentClosures = new ArrayList<String>();
		Scope cParent = node.getEnclosingScope();

		while (cParent != null) {
			if (cParent.getType() == org.mozilla.javascript.Token.FUNCTION) {
				parentClosures.add(getFunctionNodeName((FunctionNode) cParent));
			} else if (cParent.getType() == org.mozilla.javascript.Token.SCRIPT) {
				// Top level of JavaScript file
				parentClosures.add(getScopeName());
			}
			cParent = cParent.getEnclosingScope();
		}
		if (parentClosures.size() == 0) {
			parentClosures.add(getScopeName());
		}
		return parentClosures;
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

	private String getIdentifier(String variableName, AstNode node) {
		String[] localVariables;
		boolean found = false;
		FunctionNode enclosingFunction = node.getEnclosingFunction();
		ArrayList<String> scopePath = new ArrayList<String>();
		Iterator<String> scopeIt;
		String returnMe = "";

		while (enclosingFunction != null) {
			scopePath.add(getFunctionName(enclosingFunction));
			localVariables = getVariablesNamesInFunction(enclosingFunction);

			for (int j = 0; j < localVariables.length; j++) {
				if (localVariables[j].equals(variableName)) {
					found = true;
					break;
				}
			}

			if (found) {
				scopeIt = scopePath.iterator();
				while (scopeIt.hasNext()) {
					returnMe += scopeIt.next() + "-";
				}
				return returnMe + variableName;
			}
			enclosingFunction = node.getEnclosingFunction();
		}  
		return "global-" + variableName;
	}

	// TODO: mark left side with scope + variable name (right now its just scope but we are only interested in that
	//       given variable at the most local level)
	//ArrayList<String> myClosure = getClosure(node);
	private void handleVariableDeclaration(VariableDeclaration node) {
		List<VariableInitializer> vi = node.getVariables();
		Iterator<VariableInitializer> varIt = vi.iterator();
		VariableInitializer nextInitializer;
		AstNode leftSide;
		AstNode rightSide;
		AstNode newRightSide;
		String newBody;

		System.out.println("[handleVariableDeclaration]: begin");

		while (varIt.hasNext()) {
			nextInitializer = varIt.next();
			leftSide = nextInitializer.getTarget();
			rightSide = nextInitializer.getInitializer();

			if (rightSide == null) {
				// Variable declaration without assignment e.g. "var i;"
				continue;
			}

			// Just in case
			newBody = rightSide.toSource();

			//System.out.println(Token.typeToName(rightSide.getType()));


			if (leftSide.getType() == org.mozilla.javascript.Token.NAME && isItInteresting(((Name) leftSide).getIdentifier(), leftSide.getLineno())) {// ((Name) leftSide).getIdentifier().equals(variableName)) {
				//	newBody = ("("+VARWRITE+"(\""+leftSide.toSource()+"\", "+node.getLineno()+"), "+rightSide.toSource()+")");

				String newAlias = "\"\"";
				if (rightSide.getType() == org.mozilla.javascript.Token.NAME
						|| rightSide.getType() == org.mozilla.javascript.Token.GETPROP) {
					newAlias = "\""+ rightSide.toSource().replaceAll("\\\"", "'") +"\"";
				} else if (rightSide.getType() == org.mozilla.javascript.Token.CALL) {
					newAlias = "\""+ ((FunctionCall) rightSide).getTarget().toSource() +"\"";
				}

				newBody = (VARWRITE+"(\""+leftSide.toSource()+"\", " +rightSide.toSource()+ " , "+ newAlias +", "+node.getLineno()+", \""+this.getScopeName()+"\")");

				/*System.out.println("Variable declaration:");
				System.out.println(((Name) leftSide).getIdentifier() + " at line number: " + node.getLineno());
				System.out.println(Token.typeToName(rightSide.getType()));*/

			} else if (rightSide.getType() == org.mozilla.javascript.Token.NAME 
					&& isItInteresting(((Name) rightSide).getIdentifier(), rightSide.getLineno())) {
				//	&& ((Name) rightSide).getIdentifier().equals(variableName)) {

				newBody = (VARWRITE+"(\""+leftSide.toSource()+"\", "+rightSide.toSource()+", \"" +rightSide.toSource().replaceAll("\\\"", "'")+ "\" ,"+node.getLineno()+", \""+this.getScopeName()+"\")");

				// TODO: Add left side to interesting variables
				Name related = new Name();
				related.setLineno(node.getLineno()+1);
				related.setIdentifier(leftSide.toSource());

			} else if (rightSide.getType() == org.mozilla.javascript.Token.GETPROP 
					//&& ((PropertyGet) rightSide).getTarget().toSource().equals(variableName)) {
					&& isItInteresting(((PropertyGet) rightSide).getTarget().toSource().split("\\.")[0], rightSide.getLineno())) {   

				newBody = (VARWRITE+"(\""+leftSide.toSource()+"\", " +rightSide.toSource()+ ", \"" +rightSide.toSource().replaceAll("\\\"", "'")+ "\" ,"+node.getLineno()+", \""+this.getScopeName()+"\")");

				// TODO: Add left side to interesting variables
				Name related = new Name();
				related.setLineno(node.getLineno()+1);
				related.setIdentifier(leftSide.toSource());



			}
			/*SimpleSearcher ss = new SimpleSearcher();
			ss.setVariableName(variableName);
			ss.visit(rightSide);
			boolean found = ss.getFound();

			System.out.println("Element found in here? " + found);*/


			newRightSide = parse(newBody, node.getLineno());
			if (newRightSide != null) {
				nextInitializer.setInitializer(newRightSide);
			}
			System.out.println("[handleVariableDeclaration]: end");
		}
	}

	private void handleName(Name node) {
		String newBody;
		AstNode newTarget;
		AstNode parent = node.getParent();

		String parentFnName = getParentFunctionName(node.getIdentifier(), node.getLineno());

		if (node.getParent() != null) {
			if (isLeftOfAssignment(node) && node.getParent() instanceof InfixExpression) {
				if (((InfixExpression) node.getParent()).getLeft().equals(node)) {
					// Don't want to instrument LHS of assignment since it messes up the assignment 
					// (function return value cant be assigned a value)
					return;
				}
			} else if (node.getParent().getType() == org.mozilla.javascript.Token.FUNCTION) {
				// Don't want to instrument arguments in a function declaration
				return;
			} else if (parent.getType() == org.mozilla.javascript.Token.INC || parent.getType() == org.mozilla.javascript.Token.DEC) {

				//_dynoWrite('ss_cur', 0, '', 3, '/phorm.js');
				newBody = VARWRITE+"(\'"+node.getIdentifier()+"\',"
						+ node.toSource()+", " 
						+node.getLineno()+", \"" 
						+this.getScopeName()+"\" , "
						+"\"" + parentFnName + "\")";

				return;
			}
		}


		if (node.getParent().getType() == org.mozilla.javascript.Token.GETPROP
				&& !isLeftOfAssignment(node)) {

			// If leading name/label e.g. 'document' in 'document.getElement()'
			if (parent.toSource().split("\\.")[0].equals(node.getIdentifier())) {
				newBody = VARREAD+"(\'"+node.getIdentifier()+"\',"
						+ node.getIdentifier() +", "
						+node.getLineno()+", \"" 
						+this.getScopeName()+"\", "
						+"\"" + parentFnName + "\")";
			} else {
				newBody = parent.toSource().replaceFirst("."+node.getIdentifier(), "["+PROPREAD+"(\""+node.getIdentifier()+"\", "+node.getLineno()+", \"" +this.getScopeName()+"\")]");
			}
		} else if (node.getParent().getType() != org.mozilla.javascript.Token.VAR && !isLeftOfAssignment(node)
				&& node.getParent().getType() != org.mozilla.javascript.Token.CATCH) {
			newBody = VARREAD+"(\'"+node.getIdentifier()+"\',"
					+ node.getIdentifier()+", " +node.getLineno()+", \""
					+this.getScopeName()+"\", "
					+"\"" + parentFnName + "\")";
		} else {
			return;
		}
		newTarget = parse(newBody, node.getLineno());
		if (newTarget != null) {
			node.setIdentifier(newBody);
		}
	}

	private void handleArgumentName(Name node, String functionName, int index) {
		String newBody = node.getIdentifier();
		AstNode newTarget;
		AstNode parent = node.getParent();

		String parentFnName = getParentFunctionName(node.getIdentifier(), node.getLineno());

		if (node.getParent().getType() == org.mozilla.javascript.Token.GETPROP) {
			// If leading name/label e.g. 'document' in 'document.getElement()'
			if (parent.toSource().split("\\.")[0].equals(node.getIdentifier())) {

				newBody = ARGREAD+"(\'"+node.getIdentifier()+"\',"+ node.getIdentifier() +",\""+functionName+ "\"," +index+", "+node.getLineno()+", \""+this.getScopeName()+"\", \""+parentFnName+"\")";
			} /*else {
				newBody = parent.toSource().replaceFirst("."+node.getIdentifier(), "["+ARGREAD+"(\""+node.getIdentifier()+"\", "+index+ ", "+node.getLineno()+")]");
			}*/
		} else if (node.getParent().getType() != org.mozilla.javascript.Token.VAR) {
			newBody = ARGREAD+"(\'"+node.getIdentifier()+"\',"+ node.getIdentifier()+",\""+functionName+ "\","+index+", " +node.getLineno()+", \""+this.getScopeName()+"\", \"" + parentFnName + "\")";
		} else {
			return;
		}
		newTarget = parse(newBody, node.getLineno());
		if (newTarget != null) {
			node.setIdentifier(newBody);
		}
	}

	private void handleProperty(PropertyGet node) {
		if (node.getProperty().toSource().indexOf("[") == 0) {
			System.out.println("First character is square bracket!");
			return;
		}

		String varBeingWritten = node.getLeft().toSource().split("\\.")[0];

		if (!isItInteresting(varBeingWritten, node.getLeft().getLineno())) {
			return;
		} else if (node.getParent() != null 
				&& (node.getParent().getType() == org.mozilla.javascript.Token.ASSIGN
				||node.getParent().getType() == org.mozilla.javascript.Token.ASSIGN_ADD
				|| node.getParent().getType() == org.mozilla.javascript.Token.ASSIGN_SUB)
				&& node.equals(((InfixExpression) node.getParent()).getLeft())) {
			return;
		}

		AstNode newTarget;
		String newBody = node.getProperty().toSource();

		if (node.getParent().getType() == org.mozilla.javascript.Token.CALL 
				&& node.getParent().toSource().indexOf(node.toSource()) == 0) {

			// August 12, this is where 'open' and 'send' are instrumented
			newBody = "["+FUNCCALL+"(\""
					+node.getTarget().toSource()+"\", \""
					+node.getProperty().toSource()+"\", "
					+node.getLineno()
					+", \"" +this.getScopeName()+"\")]";
		} else if (!isLeftOfAssignment(node)) {
			newBody = "["+PROPREAD+"(\""
					+node.getTarget().toSource()+"\", \""
					+node.getProperty().toSource()+"\", "
					+node.getLineno()
					+", \"" +this.getScopeName()+"\")]";
		}



		// Check if valid JavaScript to be safe
		newTarget = parse(newBody, node.getLineno());

		System.out.println("[handleProperty] : end");


		Name tt = new Name();
		tt.setIdentifier(newBody);
		node.setProperty(tt);


	}

	private void handleArgumentGetProp(PropertyGet node, String functionName, int index) {
		if (node.getProperty().toSource().indexOf("[") == 0) {
			System.out.println("First character is square bracket!");
			return;
		}

		System.out.println("[handleProperty] : begin");

		String varBeingWritten = node.getLeft().toSource().split("\\.")[0];

		if (!isItInteresting(varBeingWritten, node.getLeft().getLineno())) {
			return;
		}

		AstNode newTarget;

		String variableName = node.getTarget().toSource();


		handleProperty(node);


		//String newBody = ARGREAD+"(\""+variableName+"\", "+node.toSource()+",\""+functionName+ "\","+index+", "+node.getLineno()+")";


		//		System.out.println(newBody);

		// ORIGINAL
		//String newBody = "["+PROPREAD+"(\""+node.getTarget().toSource()+"\", \""+node.getProperty().toSource()+"\", "+index+", "+node.getLineno()+")]";

		// CANT DO THIS SINCE PROPERTY MUST BE RETURNED AS STRING --> bunny["prop"], value cant be second argument




		//String newBody = PROPREAD+"(\""+node.toSource()+"\", "+node.toSource()+", "+node.getLineno()+")";


		// Check if valid JavaScript to be safe
		//		newTarget = parse(newBody, node.getLineno());

		//		System.out.println("[handleProperty] : end");
		//		System.out.println(newBody);


		Name tt = new Name();
		//		tt.setIdentifier(newBody);
		//		node.setProperty(tt);


	}



	private void handleAssignmentOperator(Assignment node) {
		// Left & Right side
		AstNode leftSide = node.getLeft();
		AstNode rightSide = node.getRight();
		int rightRightSideType = rightSide.getType();

		// Replacement holders
		AstNode newRightSide;
		String newBody = "";
		String varBeingWritten = "";
		String varBeingRead = "";

		ArrayList<String> wrapperArgs = new ArrayList<String>();

		if (leftSide.getType() == org.mozilla.javascript.Token.GETPROP) {
			varBeingWritten = leftSide.toSource().split("\\.")[0];
		} else if (leftSide.getType() == org.mozilla.javascript.Token.NAME) {
			varBeingWritten = ((Name) leftSide).getIdentifier();
		} else if (leftSide.getType() == org.mozilla.javascript.Token.THIS) {
			varBeingWritten = "this";
		}

		if (rightSide.getType() == org.mozilla.javascript.Token.GETPROP) {
			varBeingRead = rightSide.toSource().split("\\.")[0];
		} else if (rightSide.getType() == org.mozilla.javascript.Token.NAME) {
			varBeingRead = ((Name) rightSide).getIdentifier();
		} else if (rightSide.getType() == org.mozilla.javascript.Token.THIS) {
			varBeingRead = "this";
		}

		System.out.println("last thing:" + Token.typeToName(node.getOperator()));

		// newBody = (VARWRITE+"(\""+leftSide.toSource()+"\", \""+rightSide.toSource()+"\", " +rightSide.toSource()+ " ,"+node.getLineno()+")");

		if (isItInteresting(varBeingWritten, leftSide.getLineno()) || isItInteresting(varBeingRead, rightSide.getLineno())){ 
			//		varBeingWritten.equals(variableName)
			//		|| varBeingRead.equals(variableName)) {


			// Variable of interest is being written to
			if (rightRightSideType == org.mozilla.javascript.Token.FUNCTION) {
				wrapperArgs.add(leftSide.toSource());
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add("\""+getFunctionName((FunctionNode) rightSide)+"\"");
				wrapperArgs.add(node.getLineno()+"");
				wrapperArgs.add("\""+this.getScopeName()+"\"");
				newBody = generateWrapper(VARWRITE, wrapperArgs);
			} else if ((rightRightSideType == org.mozilla.javascript.Token.STRING 
					|| rightRightSideType == org.mozilla.javascript.Token.NUMBER
					|| rightRightSideType == org.mozilla.javascript.Token.NEG
					|| rightRightSideType == org.mozilla.javascript.Token.POS
					|| rightRightSideType == org.mozilla.javascript.Token.NULL
					// More options
					|| rightRightSideType == org.mozilla.javascript.Token.GETPROP
					|| rightRightSideType == org.mozilla.javascript.Token.NOT
					|| rightRightSideType == org.mozilla.javascript.Token.NAME
					|| rightRightSideType == org.mozilla.javascript.Token.FALSE
					|| rightRightSideType == org.mozilla.javascript.Token.TRUE
					|| rightRightSideType == org.mozilla.javascript.Token.OBJECTLIT
					|| rightRightSideType == org.mozilla.javascript.Token.THIS)
					// Must be an assign if right side is variable of interest
					&& ((node.getOperator() == org.mozilla.javascript.Token.ASSIGN
					&& isItInteresting(varBeingRead, rightSide.getLineno())) ||
					// If not sure assign (e.g. += or -=, no new reference is created, no need to watch line)
					(isItInteresting(varBeingWritten, rightSide.getLineno())))) {




				// First argument is the variable being written to, LHS
				wrapperArgs.add(leftSide.toSource());
				// Second argument is the value being written to, RHS (no quotes)
				wrapperArgs.add(rightSide.toSource());


				if (rightRightSideType == org.mozilla.javascript.Token.NAME || rightRightSideType == org.mozilla.javascript.Token.GETPROP) {
					// RHS name or getProp, 3rd argument is RHS (with quotes)
					wrapperArgs.add("\"" +rightSide.toSource().replaceAll("\\\"", "'")+ "\"");
				} else if (rightRightSideType == org.mozilla.javascript.Token.CALL) {
					// TODO: NOT REACHABLE

					wrapperArgs.add("\""+((FunctionCall) rightSide).getTarget().toSource()+"\"");
				} else if (rightRightSideType == org.mozilla.javascript.Token.THIS)  {
					// RHS is 'this', 3rd argument is 'this'

					wrapperArgs.add("\"this\"");
				} else {
					// Otherwise, third argument is nothing, probably primitive assignment

					wrapperArgs.add("\"\"");
				}
				wrapperArgs.add(node.getLineno()+"");
				wrapperArgs.add("\""+this.getScopeName()+"\"");

				if (rightRightSideType == org.mozilla.javascript.Token.ADD
						|| rightRightSideType == org.mozilla.javascript.Token.SUB) {
					handleInfix((InfixExpression) rightSide);
				} else if (rightRightSideType == org.mozilla.javascript.Token.GETPROP) {
					handleProperty((PropertyGet) rightSide);
				} else if (rightRightSideType == org.mozilla.javascript.Token.THIS)  {

					String parentFnName = getParentFunctionName(varBeingWritten, node.getLineno());

					node.setRight(parse(VARREAD+"(\'this\', this, "+node.getLineno()+", \""+getScopeName()+"\", \""+parentFnName+"\")", node.getLineno()));


				} else if (rightRightSideType == org.mozilla.javascript.Token.NAME) {
					handleName((Name) rightSide);
				}


				//newBody = rightSide.toSource().replaceFirst(rightSide.toSource(), generateWrapper(VARWRITE, wrapperArgs));
				if (node.getType() == org.mozilla.javascript.Token.ASSIGN) {
					newBody = generateWrapper(VARWRITE, wrapperArgs);
				} else {
					// ASSIGN_ADD, ASSIGN_SUB
					newBody = generateWrapper(VARWRITEAUG, wrapperArgs);
				}
			} else if (rightRightSideType == org.mozilla.javascript.Token.ADD) {
				// Need to iterate through all add items so we can backwards slice from those
				// These include string concats
				wrapperArgs.add(leftSide.toSource());
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add("\"\"");
				wrapperArgs.add(node.getLineno()+"");
				wrapperArgs.add("\""+this.getScopeName()+"\"");

				newBody = generateWrapper(VARWRITE, wrapperArgs);
				if (node.getType() == org.mozilla.javascript.Token.ASSIGN) {
					newBody = generateWrapper(VARWRITE, wrapperArgs);
				} else {
					// ASSIGN_ADD, ASSIGN_SUB
					newBody = generateWrapper(VARWRITEAUG, wrapperArgs);
				}   
			} else if (rightRightSideType == org.mozilla.javascript.Token.SUB) { 
				// Need to iterate through all items involve din the subtraction
				wrapperArgs.add(leftSide.toSource());
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add("\"\"");
				wrapperArgs.add(node.getLineno()+"");
				wrapperArgs.add("\""+this.getScopeName()+"\"");

				newBody = generateWrapper(VARWRITE, wrapperArgs);
				if (node.getType() == org.mozilla.javascript.Token.ASSIGN) {
					newBody = generateWrapper(VARWRITE, wrapperArgs);
				} else {
					// ASSIGN_ADD, ASSIGN_SUB
					newBody = generateWrapper(VARWRITEAUG, wrapperArgs);
				} 
			} else if (rightRightSideType == org.mozilla.javascript.Token.CALL
					&& ((FunctionCall) rightSide).getTarget().toSource().indexOf(TOOLNAME) == -1) {
				// Need to iterate through arguments to get data depends
				// Function being called may provide the control flow? and return type (therefore include the return statement in the slice?)
				wrapperArgs.add(leftSide.toSource());
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add("\""+((FunctionCall) rightSide).getTarget().toSource()+"\"");

				wrapperArgs.add(node.getLineno()+"");
				wrapperArgs.add("\""+this.getScopeName()+"\"");

				newBody = generateWrapper(VARWRITEFUNCRET, wrapperArgs);

			} else if (rightRightSideType == org.mozilla.javascript.Token.NEW) {
				wrapperArgs.add(leftSide.toSource());
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add("\"\"");
				wrapperArgs.add(node.getLineno()+"");
				wrapperArgs.add("\""+this.getScopeName()+"\"");

				newBody = ("("+VARWRITE+"(\""+leftSide.toSource()+"\", "+rightSide.toSource()+",\"\","+node.getLineno()+", \"" + getScopeName()+"\"), "+rightSide.toSource()+")");
			} else if (rightRightSideType == org.mozilla.javascript.Token.CALL
					&& ((FunctionCall) rightSide).getTarget().toSource().indexOf(TOOLNAME) != -1) {
				return;
			} else {
				System.out.println("New right side type:" + Token.typeToName(rightRightSideType));
			}


			if (newBody.length() > 0) {
				node.setRight(parse(newBody, node.getLineno()));
			}
		} else if (isItInteresting(varBeingRead, rightSide.getLineno())) {
			// TODO: Thursday, March 6th, 2014
			// If variable of interest is not a primitive type (is a complex object), it could be changed by reference. Therefore, the variable being written to is a dependency?

		}
	}

	private String generateWrapper (String wrapperMethod, ArrayList<String> arguments) {
		String toBeReturned = wrapperMethod + "(";
		Iterator<String> it = arguments.iterator();
		String nextArgument;
		boolean first = true;

		while (it.hasNext()) {
			nextArgument = it.next();
			if (first) {
				nextArgument = "\"" + nextArgument.replaceAll("\"", "\'") + "\"";
				first = false;
			} else {
				nextArgument = ", " + nextArgument;
			}
			toBeReturned += nextArgument;
		}
		toBeReturned += ")";

		return toBeReturned;
	}

	private void updateAllLineNo(AstNode body) {

		AstNode lastChild = (AstNode) body.getLastChild();

		if (lastChild == null) {
			// No children
			return;
		}

		while (true) {
			// Update line number of immediate children
			lastChild.setLineno(lastChild.getLineno()+body.getLineno());

			// Call recursively for grandchildren, greatgrandchildren, etc.
			updateAllLineNo(lastChild);

			if (body.getChildBefore(lastChild) != null) {
				lastChild = (AstNode) body.getChildBefore(lastChild);
			} else {
				break;
			}
		} 
	}

	private void handleFunctionCall(FunctionCall node) {

		if (node.getTarget().toSource().indexOf(TOOLNAME) != -1) {
			// We don't want to instrument out code (dirty way)
			return;
		}

		boolean isTargetInArgument = false;

		// Store information on function calls
		AstNode target = node.getTarget();
		String targetMethod = target.toSource();
		String targetObject = "";
		String[] dotSplit;

		AstNode newTarget = null;
		String newBody;

		List<AstNode> args = node.getArguments();
		Iterator<AstNode> argsIt = args.iterator();
		AstNode nextArg;
		int i = 0;

		// TODO: fix this
		int lineNo = (node.getLineno() == 0 ? node.getParent().getParent().getLineno() : 0);




		// Check arguments passed into function call

		for (int j = 0; j < node.getArguments().size(); j++) {
			nextArg = node.getArguments().get(j);

			//while (argsIt.hasNext()) {
			//nextArg = argsIt.next();
			dotSplit = nextArg.toSource().split("\\.");


			// Argument is variable of interest
			if (nextArg.getType() == org.mozilla.javascript.Token.NAME && isItInteresting(((Name) nextArg).getIdentifier(), nextArg.getLineno())) {
				handleArgumentName((Name) nextArg, targetMethod, i);


				isTargetInArgument = true;

			} else if (nextArg.getType() == org.mozilla.javascript.Token.THIS && isItInteresting("this", nextArg.getLineno())) {
				// adding support for this


				String parentFnName = getParentFunctionName("this", node.getLineno());

				newBody = ARGREAD+"(\'this\', this,\""+targetMethod+ "\"," +i+", "+node.getLineno()+", \""+this.getScopeName()+"\", \""+parentFnName+"\")";

				nextArg = parse(newBody, node.getLineno());
				System.out.println("NEXTARG: " + nextArg.toSource());
				args.remove(i);
				args.add(i, nextArg);
				nextArg.setParent(node);

			} else if (nextArg.getType() == org.mozilla.javascript.Token.GETPROP && isItInteresting(dotSplit[0], nextArg.getLineno())) {

				// Portion of target variable is being passed as argument, could be modified in function, must instrument function for whichever argument we want to track

				String untouchedID = ((PropertyGet) nextArg).getTarget().toSource();


				//handleArgumentGetProp((PropertyGet) nextArg, targetMethod, i);
				handleProperty((PropertyGet) nextArg);


				//	newBody = nextArg.toSource() + ")";

				//	newBody = "("+VARWRITEFUNCRET+"('"+((FunctionCall) nextArg).getTarget().toSource()+"')," + newBody;
				String parentFnName = getParentFunctionName(dotSplit[0], node.getLineno());

				newBody = ARGREAD+"(\'"+untouchedID.replace("\'", "\"")+"\',"+ nextArg.toSource() +",\""+targetMethod+ "\"," +i+", "+node.getLineno()+", \""+this.getScopeName()+"\", \""+parentFnName+"\")";


				nextArg = parse(newBody, node.getLineno());
				System.out.println("NEXTARG: " + nextArg.toSource());
				args.remove(i);
				args.add(i, nextArg);
				nextArg.setParent(node);

				isTargetInArgument = true;

			} else if (org.mozilla.javascript.Token.CALL == nextArg.getType()
					&& dotSplit.length > 1
					&& isItInteresting(dotSplit[0], nextArg.getLineno())) {




				isTargetInArgument = true;

			} else if (org.mozilla.javascript.Token.CALL == nextArg.getType()
					&& isAnArgument(node)) {

				newBody = nextArg.toSource() + ", \"" + this.getScopeName() + "\")";

				newBody = "("+VARWRITEFUNCRET+"('"+((FunctionCall) nextArg).getTarget().toSource()+"')," + newBody;

				nextArg = parse(newBody, node.getLineno());
				System.out.println("NEXTARG: " + nextArg.toSource());
				args.remove(i);
				args.add(i, nextArg);
				nextArg.setParent(node);

				//fnCall.set
			}

			i++;
		}




		if (!isTargetInArgument) {
			return;
		}


		// Maybe we dont need to wrap the actual call, just the declaration

		// this should be done in DependencyFinder

		int tt = target.getType();
		if (tt == org.mozilla.javascript.Token.GETPROP) {
			// Class specific function call, 33
			// E.g. document.getElementById, e.stopPropagation

			String[] methods = targetMethod.split("\\.");
			targetObject = methods[0];
			targetMethod = methods[methods.length-1];

			if (isItInteresting(targetObject, target.getLineno())) {

				//	newBody = target.toSource().replaceFirst("."+targetMethod, "["+FUNCCALL+"(\""+((PropertyGet) target).getLeft().toSource()+"\",\""+targetMethod+"\", "+target.getLineno()+")]");


				// Add 'targetMethod' to functions to instrument entry

			}

		} else if (node.getType() == org.mozilla.javascript.Token.CALL && isTargetInArgument) {
			// Add 'newBody' to functions to instrument (closest in scope tree)


			// DOWNWARDS

		} 

		/*	int tt = target.getType();
		if (tt == org.mozilla.javascript.Token.GETPROP) {
			// Class specific function call, 33
			// E.g. document.getElementById, e.stopPropagation

			String[] methods = targetMethod.split("\\.");
			targetObject = methods[0];
			targetMethod = methods[methods.length-1];

			if (variableName.equals(targetObject)) {

				newBody = target.toSource().replaceFirst("."+targetMethod, "["+FUNCCALL+"(\""+((PropertyGet) target).getLeft().toSource()+"\",\""+targetMethod+"\", "+target.getLineno()+")]");
				newTarget = parse(newBody);
				newTarget.setLineno(target.getLineno());

			}

		} else if (node.getType() == org.mozilla.javascript.Token.CALL && isTargetInArgument) {
			newBody = target.toSource();
			newBody = target.toSource().replaceFirst(newBody, FUNCCALL+"('"+newBody+"',"+newBody+","+lineNo+")");
			newTarget = parse(newBody);

		} 





		if (newTarget != null) {
			node.setTarget(newTarget);
		}*/
	}


	private void handleUnaryExpression(UnaryExpression node) {
		AstNode operand = node.getOperand();
		String newBody;
		
		System.out.println(Token.typeToName(node.getType()));

		if (isItInteresting(node.getOperand().toSource(), node.getLineno())) {
			if (node.toSource().indexOf("++") == 0 || node.toSource().indexOf("--") == 0) {
				newBody = operand.toSource()+", "+VARWRITEAUG+"(\'"+operand.toSource()+"\',"+ operand.toSource()+", \'\'," +node.getLineno()+", \"" +this.getScopeName()+"\")";
			} else {
				newBody = VARWRITEAUG+"(\'"+operand.toSource()+"\',"+ operand.toSource()+", \'\'," +node.getLineno()+", \"" +this.getScopeName()+"\"), "+operand.toSource();
			}

			/*parent.set
		node.setIdentifier(newBody);*/

			//System.out.println(parent.getClass());
			node.setOperand(parse(newBody, node.getLineno()));
		}

	}

	private void handleReturn(ReturnStatement node) {
		// return statements

		int lineNo = node.getLineno()+1;
		AstNode newRV;

		if (node.getReturnValue() != null) {
			// Wrap return value
			newRV = parse("RSW("+ node.getReturnValue().toSource() + ", '" + node.getReturnValue().toSource().replace("'", "\\'")+ "' ," + lineNo +");");
			//			newRV = parse("RSW("+ node.getReturnValue().toSource() + ", '" + 'a' + "' ," + lineNo +");");
			//			newRV = parse("RSW("+ node.getReturnValue().toSource() + ", \"val\" ," + lineNo +");");
			newRV.setLineno(node.getReturnValue().getLineno());

		} else {
			// Return value is void
			newRV = parse("RSW(" + lineNo +")");
			newRV.setLineno(node.getLineno());
		}

		updateAllLineNo(newRV);
		node.setReturnValue(newRV);
	}

	private boolean isAnArgument(FunctionCall node) {

		ArrayList<AstNode> argumentNames = FunctionCallParser.getArgumentDependencies(node);
		Iterator<AstNode> argumentIterator = argumentNames.iterator();
		AstNode nextArgument;
		boolean found = false;

		while (argumentIterator.hasNext()) {
			nextArgument = argumentIterator.next();
			if (nextArgument instanceof Name && isItInteresting(((Name) nextArgument).getIdentifier(), nextArgument.getLineno())) {
				found = true;
				break;
			} else if (nextArgument instanceof KeywordLiteral && nextArgument.toSource().equals("this") && isItInteresting("this", nextArgument.getLineno())) {
				found = true;
				break;
			}
		}

		return found;
	}

	boolean fromHere = false;

	private void handleGetElem(ElementGet node) {
		System.out.println("handleGetElem:");

		fromHere = true;
	}

	private void handleInfix (InfixExpression node) {
		ArrayList<AstNode> operands = new ArrayList<AstNode>();
		AstNode operand;
		Iterator<AstNode> it;
		ArrayList<AstNode> d = new ArrayList<AstNode>();

		// Un-used right now
		//int operationType = node.getOperator();

		operands.add(node.getLeft());
		operands.add(node.getRight());

		it = operands.iterator();

		while (it.hasNext()) {
			operand = it.next();
			switch (operand.getType()) {
			case org.mozilla.javascript.Token.ADD:  
				// Call recursively (e.g. var a = b + c + d)
				handleInfix((InfixExpression) operand);
				break;
			case org.mozilla.javascript.Token.SUB:
				handleInfix((InfixExpression) operand);
				break;
			case org.mozilla.javascript.Token.NAME:  
				d.add(operand);
				handleName((Name) operand);
				break;
			case org.mozilla.javascript.Token.THIS:  
				d.add(operand);

				String parentFnName = getParentFunctionName("this", node.getLineno());

				if (node.getRight().equals(operand)) {
					node.setRight(parse(VARREAD+"(\'this\', this, "+node.getLineno()+", \""+getScopeName()+"\", \""+parentFnName+"\")", node.getLineno()));
				} else if (node.getRight().equals(operand)) {
					node.setRight(parse(VARREAD+"(\'this\', this, "+node.getLineno()+", \""+getScopeName()+"\", \""+parentFnName+"\")", node.getLineno()));
				}
				break;
			case org.mozilla.javascript.Token.GETPROP:  
				handleProperty((PropertyGet) operand);
				break;
			case org.mozilla.javascript.Token.NUMBER:  
			case org.mozilla.javascript.Token.STRING:  
				break;
			default:
				System.out.println("[InfixExpression]: Error parsing Infix Expression. Unknown operand type. (getNames())");
				break;
			}
		}
	}

	static private ArrayList<AstNode> dependencies = new ArrayList<AstNode>();

	public ArrayList<AstNode> getNextSliceStart() {
		return dependencies;
	}



	private ArrayList<SlicingCriteria> variablesOfInterest;
	private AstRoot astCrawled;
	public void setRoot (AstRoot ast) {
		this.astCrawled = ast;
	}

	public void setVariablesOfInterest (ArrayList<SlicingCriteria> vars, AstRoot asd) {
		this.variablesOfInterest = vars;

		Iterator<SlicingCriteria> it = vars.iterator();

		this.astCrawled = asd;
	}

	public ArrayList<SlicingCriteria> getVariablesOfInterest () {
		return this.variablesOfInterest;
	}

	private boolean isItInteresting (String varName, int lineNo) {
		if (varName == "" || varName == null) {
			System.out.println("[isItInteresting]: Invalid variable name.");
			return false;
		}

		Scope scope = TraceHelper.getDefiningScope(astCrawled, varName, lineNo);

		SlicingCriteria checkMe = new SlicingCriteria(scope, varName, true);

		Iterator<SlicingCriteria> it = this.variablesOfInterest.iterator();
		SlicingCriteria next;

		while (it.hasNext()) {
			next = it.next();

			if (checkMe.equals(next)) {
				System.out.println("[isItIteresting]: Returning true");
				return true;
			}
		}
		System.out.println("[isItIteresting]: Returning false 2");
		return false;
	}

	private boolean isLeftOfAssignment (AstNode node) {
		AstNode previousParent = node;
		AstNode parent = node.getParent();

		while (parent != null) {
			if (parent.getType() == org.mozilla.javascript.Token.ASSIGN
					|| parent.getType() == org.mozilla.javascript.Token.ASSIGN_ADD
					|| parent.getType() == org.mozilla.javascript.Token.ASSIGN_SUB) {
				if (previousParent.equals(((InfixExpression) parent).getLeft())) {
					return true;
				}   
			}   
			previousParent = parent;
			parent = parent.getParent();
		}   
		return false;
	}

	public String getParentFunctionName (String varName, int lineNo) {
		String returnMe = "global";

		Scope startScope = TraceHelper.getDefiningScope(astCrawled, varName, lineNo);

		if (startScope instanceof FunctionNode) {
			FunctionNode node = (FunctionNode) startScope;

			String name = node.getName();

			AstNode parent = node.getParent();

			if (node.getFunctionType() == FunctionNode.FUNCTION_EXPRESSION) {
				// Complicated Case
				if (node.getName() == "" && parent.getType() == org.mozilla.javascript.Token.COLON) {
					// Assignment Expression                    
					name = node.getParent().toSource().substring(0,node.getParent().toSource().indexOf(node.toSource()));
					name = name.substring(0,name.indexOf(":"));
				} else if (node.getName() == "" && parent.getType() == org.mozilla.javascript.Token.ASSIGN) {
					name = node.getParent().toSource().substring(0,node.getParent().toSource().indexOf(node.toSource()));
					name = name.substring(name.lastIndexOf(".")+1,name.indexOf("="));
				}
				name = name.trim();
			}

			returnMe = name;
		}

		return returnMe;
	}
}
