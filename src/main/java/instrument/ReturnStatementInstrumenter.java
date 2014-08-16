package instrument;

import java.util.ArrayList;
import java.util.Iterator;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;

public class ReturnStatementInstrumenter extends AstInstrumenter {
	private static final String READFUNCRET = "_dynoReturnValue";

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
	 * Construct without patterns.
	 */
	public ReturnStatementInstrumenter() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public ReturnStatementInstrumenter(ArrayList<String> excludes) {
		super(excludes);
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
		Parser p = new Parser(compilerEnvirons, errorReporter);

		System.out.println("[parsing compilerEnvirons]: ");



		code = code.replaceAll("\\;\\\n\\ \\,", ",")
				.replaceAll("\"", "\'")
				.replaceAll("\\.\\[", "[")
				.replaceAll("\\;\\\n\\)", ")");

		if (this.scopeName.indexOf("string_library") != -1) {
			System.out.println(code);
		}
		



		return p.parse(code, null, lineNo);
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


	public Scope getTopScope() {
		return topMost;
	}

	@Override
	public  boolean visit(AstNode node){
		int tt = node.getType();

		if (tt == org.mozilla.javascript.Token.RETURN) {
			// TODO:
			handleReturnStatement((ReturnStatement) node);
		}

		return true;  // process kids
	}

	private void handleReturnStatement(ReturnStatement node) {
		// Return statements
		String newRV;
		ArrayList<String> wrapperArgs = new ArrayList<String>();
		
		if (!node.getEnclosingFunction().equals(getParentFunction())
				|| (node.getReturnValue() != null
						&& node.getReturnValue().toSource().indexOf(READFUNCRET) != -1)) {
			return;
		}
		//functionName, returnValue, fileName, lineNo,
        wrapperArgs.add(getParentFunction().getName());
        wrapperArgs.add((node.getReturnValue() == null? "null" :node.getReturnValue().toSource()));
        wrapperArgs.add("\""+getScopeName()+"\"");			
        wrapperArgs.add(node.getLineno()+"");			
		
		newRV = generateWrapper(READFUNCRET, wrapperArgs);
		node.setReturnValue(parse(newRV, node.getLineno()));

		System.out.println(node.toSource());
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

		AstRoot iscNode = rhinoCreateNode(isc);

		// Return new instrumented node/code
		return iscNode;
	}

	@Override
	public void start(String node) {
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

	// Function
	private FunctionNode parentFn = null;

	public void setParentFunction (FunctionNode fn) {
		this.parentFn = fn;
	}
	public FunctionNode getParentFunction () {
		return this.parentFn;
	}
}
