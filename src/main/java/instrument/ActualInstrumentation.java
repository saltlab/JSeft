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
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.Symbol;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

import instrument.helpers.PropertyGetParser;
import units.PertinentArgument;

public class ActualInstrumentation extends AstInstrumenter{

	private static final String TOOLNAME = "_dyno";
	private static final String VARREAD = "_dynoRead";
	private static final String VARWRITE = "_dynoWrite";
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

	/**
	 * List with regular expressions of variables that should not be instrumented.
	 */
	private ArrayList<String> excludeList = new ArrayList<String>();

	private ArrayList<Name> relatedVariables = new ArrayList<Name>();

	/**
	 * Construct without patterns.
	 */
	public ActualInstrumentation() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public ActualInstrumentation(ArrayList<String> excludes) {
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

	public ArrayList<Name> getRelatedVariables () {
		return this.relatedVariables;
	}

	public void clearRelatedVariables () {
		this.relatedVariables = new ArrayList<Name>();
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

		/*	if (tt == org.mozilla.javascript.Token.GETPROP
				|| tt == org.mozilla.javascript.Token.CALL
				|| tt == org.mozilla.javascript.Token.NAME) {
			System.out.println(Token.typeToName(node.getType()) + " : " + node.toSource());
		}*/


		if (tt == org.mozilla.javascript.Token.GETPROP) {
			// TODO:
		//	handleProperty((PropertyGet) node);
		} else if (tt == org.mozilla.javascript.Token.VAR && node instanceof VariableDeclaration) {
			// TODO:
			//	handleVariableDeclaration((VariableDeclaration) node);
		} else if (tt == org.mozilla.javascript.Token.ASSIGN) {
			// TODO:
			//	handleAssignmentOperator((Assignment) node);
		} else if (tt == org.mozilla.javascript.Token.CALL
				&& !((FunctionCall) node).getTarget().toSource().contains(TOOLNAME)) {
			// TODO:

				handleFunctionCall((FunctionCall) node);
		} else if (tt == org.mozilla.javascript.Token.NAME /*&& node.getLineno() == lineNo*/ && ((Name) node).getIdentifier().equals(variableName)) {
			// TODO:

			// Might need stricter check since target variable could appear multiple times on single line
			//	handleName((Name) node);
		} else if (tt == org.mozilla.javascript.Token.FUNCTION && !node.equals(topMost) && InstrumenterHelper.isVariableLocal(variableName, (FunctionNode) node)) {
			// TODO:

			// The variable of interest is not used in the function, skip it
			return false;
		}

		return true;  // process kids
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

		return parse(code);
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
				.replaceAll("\\ \\.", " ")
				.replaceAll("(\\n\\;\\n)", "\n\n")
				//	.replaceAll("(\\n)", "\n\n")  // <-- just for spacing, might not be needed
				.replaceAll("\\.\\[", "[");

	//	System.out.println(isc);

		AstRoot iscNode = rhinoCreateNode(isc);


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

		while (varIt.hasNext()) {			
			nextInitializer = varIt.next();
			leftSide = nextInitializer.getTarget();
			rightSide = nextInitializer.getInitializer();
			// Just in case
			newBody = rightSide.toSource();


			if (leftSide.getType() == org.mozilla.javascript.Token.NAME && ((Name) leftSide).getIdentifier().equals(variableName)) {
				//	newBody = ("("+VARWRITE+"(\""+leftSide.toSource()+"\", "+node.getLineno()+"), "+rightSide.toSource()+")");

				newBody = (VARWRITE+"(\""+leftSide.toSource()+"\", " +rightSide.toSource()+ " ,"+node.getLineno()+")");

				/*System.out.println("Variable declaration:");
				System.out.println(((Name) leftSide).getIdentifier() + " at line number: " + node.getLineno());
				System.out.println(Token.typeToName(rightSide.getType()));*/

			} else if (rightSide.getType() == org.mozilla.javascript.Token.NAME 
					&& ((Name) rightSide).getIdentifier().equals(variableName)) {


				System.out.println("Right side of declarations is variable of interest");

				newBody = (VARWRITE+"(\""+leftSide.toSource()+"\", \""+rightSide.toSource()+"\", " +rightSide.toSource()+ " ,"+node.getLineno()+")");

				// TODO: Add left side to interesting variables
				Name related = new Name();
				related.setLineno(node.getLineno()+1);
				related.setIdentifier(leftSide.toSource());
				relatedVariables.add(related);

			} else if (rightSide.getType() == org.mozilla.javascript.Token.GETPROP 
					&& ((PropertyGet) rightSide).getTarget().toSource().equals(variableName)) {

				System.out.println("Right side of declarations is variable of interest (PROP)");

				newBody = (VARWRITE+"(\""+leftSide.toSource()+"\", " +rightSide.toSource()+ " ,"+node.getLineno()+")");

				// TODO: Add left side to interesting variables
				Name related = new Name();
				related.setLineno(node.getLineno()+1);
				related.setIdentifier(leftSide.toSource());
				relatedVariables.add(related);



			}
			/*SimpleSearcher ss = new SimpleSearcher();
			ss.setVariableName(variableName);
			ss.visit(rightSide);
			boolean found = ss.getFound();

			System.out.println("Element found in here? " + found);*/

			newRightSide = parse(newBody);
			if (newRightSide != null) {
				nextInitializer.setInitializer(newRightSide);
			}

		}
	}

	private void handleName(Name node) {
		String newBody;
		AstNode newTarget;
		AstNode parent = node.getParent();

		if (node.getParent().getType() == org.mozilla.javascript.Token.GETPROP) {
			// If leading name/label e.g. 'document' in 'document.getElement()'
			if (parent.toSource().split("\\.")[0].equals(node.getIdentifier())) {

				newBody = VARREAD+"(\'"+node.getIdentifier()+"\',"+ node.getIdentifier() +", "+node.getLineno()+")";
			} else {
				newBody = parent.toSource().replaceFirst("."+node.getIdentifier(), "["+PROPREAD+"(\""+node.getIdentifier()+"\", "+node.getLineno()+")]");
			}
		} else if (node.getParent().getType() != org.mozilla.javascript.Token.VAR) {
			newBody = VARREAD+"(\'"+node.getIdentifier()+"\',"+ node.getIdentifier()+", " +node.getLineno()+")";
		} else {
			return;
		}
		newTarget = parse(newBody);
		if (newTarget != null) {
			node.setIdentifier(newBody);
		}

	}

	private void handleProperty(PropertyGet node) {
		AstNode newTarget;
		ArrayList<AstNode> affectedNames = PropertyGetParser.getPropertyDependencies(node);
		Iterator<AstNode> nt = affectedNames.iterator();
		boolean nameFound = false;
		AstNode next;

		while (nt.hasNext()) {
			next = nt.next();
			if (next instanceof Name && ((Name) next).getIdentifier().equals(variableName)) {
				nameFound = true;
				break;
			} else if (next instanceof KeywordLiteral && next.toSource().equals("this") && variableName.equals("this")) {
				nameFound = true;
				break;
			}
		}



			if (nameFound) {
				String newBody = "["+PROPREAD+"(\""+node.getTarget().toSource()+"\", \""+node.getProperty().toSource()+"\", "+node.getLineno()+")]";
				newTarget = parse(newBody);
				Name tt = new Name();
				tt.setIdentifier(newBody);
				node.setProperty(tt);
			}

		



		//String newBody = PROPREAD+"(\""+node.toSource()+"\", "+node.toSource()+", "+node.getLineno()+")";


		// Check if valid JavaScript to be safe

		//	node.removeChild();
		//	node.replaceChild(node.getRight(), newTarget);
		//	node.setRight(parse(node.getProperty().toSource()));
		//	node.setRight();
		//	node.setTarget(newTarget);


		//node.setTarget(parse(""));
		//node.setOperator(org.mozilla.javascript.Token.EMPTY);
		//node.setTarget(parse("_clematestRemove"));
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
		}

		if (rightSide.getType() == org.mozilla.javascript.Token.GETPROP) {
			varBeingRead = rightSide.toSource().split("\\.")[0];
		} else if (rightSide.getType() == org.mozilla.javascript.Token.NAME) {
			varBeingRead = ((Name) rightSide).getIdentifier();
		}


		newBody = (VARWRITE+"(\""+leftSide.toSource()+"\", \""+rightSide.toSource()+"\", " +rightSide.toSource()+ " ,"+node.getLineno()+")");

		if (varBeingWritten.equals(variableName)
				|| varBeingRead.equals(variableName)) {

			if (varBeingRead.equals(variableName)) {
				// TODO: Add left side to interesting variables
				Name related = new Name();
				related.setLineno(node.getLineno()+1);
				related.setIdentifier(varBeingWritten);
				relatedVariables.add(related);
			}

			// Variable of interest is being written to
			if (rightRightSideType == org.mozilla.javascript.Token.FUNCTION) {
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(getFunctionName((FunctionNode) rightSide));
				wrapperArgs.add(node.getLineno()+"");

				newBody = generateWrapper(VARWRITE, wrapperArgs);
			} else if (rightRightSideType == org.mozilla.javascript.Token.STRING 
					|| rightRightSideType == org.mozilla.javascript.Token.NUMBER
					|| rightRightSideType == org.mozilla.javascript.Token.NEG
					|| rightRightSideType == org.mozilla.javascript.Token.POS
					|| rightRightSideType == org.mozilla.javascript.Token.NULL
					// More options
					|| rightRightSideType == org.mozilla.javascript.Token.GETPROP
					|| rightRightSideType == org.mozilla.javascript.Token.NAME
					|| rightRightSideType == org.mozilla.javascript.Token.FALSE
					|| rightRightSideType == org.mozilla.javascript.Token.TRUE
					|| rightRightSideType == org.mozilla.javascript.Token.OBJECTLIT) {
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(node.getLineno()+"");

				//newBody = rightSide.toSource().replaceFirst(rightSide.toSource(), generateWrapper(VARWRITE, wrapperArgs));
				newBody = generateWrapper(VARWRITE, wrapperArgs);
			} else if (rightRightSideType == org.mozilla.javascript.Token.ADD) {
				// Need to iterate through all add items so we can backwards slice from those
				// These include string concats
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(node.getLineno()+"");

				newBody = rightSide.toSource().replaceFirst(rightSide.toSource(), generateWrapper(VARWRITE, wrapperArgs));
			} else if (rightRightSideType == org.mozilla.javascript.Token.SUB) { 
				// Need to iterate through all items involve din the subtraction
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(node.getLineno()+"");

				newBody = rightSide.toSource().replaceFirst(rightSide.toSource(), generateWrapper(VARWRITE, wrapperArgs));
			} else if (rightRightSideType == org.mozilla.javascript.Token.CALL
					&& ((FunctionCall) rightSide).getTarget().toSource().indexOf(TOOLNAME) == -1) {
				// Need to iterate through arguments to get data depends
				// Function being called may provide the control flow? and return type (therefore include the return statement in the slice?)
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(node.getLineno()+"");

				System.out.println(((FunctionCall) rightSide).getTarget().toSource());

				newBody = generateWrapper(VARWRITEFUNCRET, wrapperArgs);
			} else if (rightRightSideType == org.mozilla.javascript.Token.NEW) {
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(node.getLineno()+"");

				newBody = ("("+VARWRITE+"(\""+varBeingWritten+"\", "+node.getLineno()+"), "+rightSide.toSource()+")");
			} else if (rightRightSideType == org.mozilla.javascript.Token.CALL
					&& ((FunctionCall) rightSide).getTarget().toSource().indexOf(TOOLNAME) != -1) {
				return;
			} else {
				System.out.println("New right side type:" + Token.typeToName(rightRightSideType));
			}

			newRightSide = parse(newBody);

			if (newRightSide != null) {
				node.setRight(newRightSide);
			}
		} else if (varBeingRead.equals(variableName)) {
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

		ArrayList<PertinentArgument> argumentsOfInterest = new ArrayList<PertinentArgument>();



		// Check arguments passed into function call
		while (argsIt.hasNext()) {
			nextArg = argsIt.next();
			dotSplit = nextArg.toSource().split("\\.");

			// Argument is variable of interest
			if (nextArg.getType() == org.mozilla.javascript.Token.NAME && ((Name) nextArg).getIdentifier().equals(variableName)) {
				handleName((Name) nextArg);

				PertinentArgument pertArg = new PertinentArgument(i, variableName);
				argumentsOfInterest.add(pertArg);
				isTargetInArgument = true;

			} else if (nextArg.getType() == org.mozilla.javascript.Token.GETPROP && dotSplit[0].equals(variableName)) {

				// Portion of target variable is being passed as argument, could be modified in function, must instrument function for whichever argument we want to track


				PertinentArgument pertArg = new PertinentArgument(i, variableName);
				for (int j = 1; j < dotSplit.length; j++) {
					pertArg.addProperty(dotSplit[j]);
				}

				argumentsOfInterest.add(pertArg);


				handleProperty((PropertyGet) nextArg);

				isTargetInArgument = true;

			} else if (org.mozilla.javascript.Token.CALL == nextArg.getType()
					&& dotSplit.length > 1
					&& dotSplit[0].equals(variableName)) {


				PertinentArgument pertArg = new PertinentArgument(i, variableName);


				for (int j = 1; j < dotSplit.length; j++) {

					pertArg.addProperty(dotSplit[j]);
				}


				argumentsOfInterest.add(pertArg);

				isTargetInArgument = true;

			}

			i++;
		}






		if (!isTargetInArgument) {
			return;
		}




		int tt = target.getType();
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

		} else if (node.getType() == org.mozilla.javascript.Token.CALL) {
			newBody = target.toSource();
			newBody = target.toSource().replaceFirst(newBody, FUNCCALL+"('"+newBody+"',"+newBody+","+lineNo+")");
			newTarget = parse(newBody);

		} 





		if (newTarget != null) {
			node.setTarget(newTarget);
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

	static private ArrayList<AstNode> dependencies = new ArrayList<AstNode>();

	public ArrayList<AstNode> getNextSliceStart() {
		return dependencies;
	}

	private String variableName = null;

	public void setVariableName (String name) {
		this.variableName = name;
	}

	public String setVariableName () {
		return this.variableName;
	}
}
