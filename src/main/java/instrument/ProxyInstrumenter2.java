package instrument;

import java.util.ArrayList;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Scope;

public class ProxyInstrumenter2 extends AstInstrumenter {

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
	private String src = "";

	private Scope lastScopeVisited = null;

	/**
	 * Construct without patterns.
	 */
	public ProxyInstrumenter2() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public ProxyInstrumenter2(ArrayList<String> excludes) {
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
	public AstRoot parse(String code) {
		Parser p = new Parser(compilerEnvirons, errorReporter);

		return p.parse(code, null, 0);
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

	static private int lineNo = -1;

	public void setLineNo (int num) {
		this.lineNo = num/*-1*/;
	}

	static private ArrayList<AstNode> dependencies = new ArrayList<AstNode>();

	public ArrayList<AstNode> getNextSliceStart() {
		return dependencies;
	}

	static private String variableName = null;

	public void setVariableName (String name) {
		this.variableName = name;
		firstTime = true;
		lastScopeVisited = null;
	}

	public Scope getLastScopeVisited () {
		return lastScopeVisited;
	}

	private boolean firstTime = true;

	@Override
	public  boolean visit(AstNode node){

		firstTime = false;

		boolean continueToChildren = true;
		int tt = node.getType();
		Scope definingScope = null;

		if (tt == org.mozilla.javascript.Token.NAME
				&& node.getLineno() == lineNo
				&& ((Name) node).getIdentifier().equals(variableName)) {
			// Starting point of slice


			definingScope = InstrumenterHelper.getDefiningScope((Name) node);
			

			if (definingScope.getType() == org.mozilla.javascript.Token.SCRIPT) {
				// Assume variable is defined in another JavaScript file and is therefore global

			}
			// At this point the defining scope should be identified, whether its global or a parent function

			// Below, replace getEnclosingFunction with a variable based on above if statement 

			/**     if (node.getEnclosingFunction() != null) {
                if (((Name) node).getDefiningScope() != node.getEnclosingFunction()) {
                    // TROUBLE!
                    System.out.println("TROUBLE 1");

                } else {
                    System.out.println("SIMPLE 1");

                }
            } else {
                // global? need to test
                System.out.println(Token.typeToName(node.getEnclosingScope().getType()));
                if (((Name) node).getDefiningScope() != node.getEnclosingScope()) {
                    // TROUBLE!
                    System.out.println("TROUBLE 2");
                } else {
                    System.out.println("SIMPLE 2");

                }
            }*/
			this.lastScopeVisited = definingScope;

			return false;

		} else if (tt == org.mozilla.javascript.Token.THIS
				&& node.getLineno() == lineNo
				&& variableName.equals("this")) {
			// Shouldn't be void/null?

			ArrayList<Scope> lookingForFn = InstrumenterHelper.getScopeChain(node);
			for (int i = 0; i < lookingForFn.size(); i++) {
				if (lookingForFn.get(i) instanceof FunctionNode) {
					definingScope = lookingForFn.get(i);
					break;
				}
			}

			this.lastScopeVisited = definingScope;

			return false;
		} else if (tt == org.mozilla.javascript.Token.EXPR_VOID
				|| tt == org.mozilla.javascript.Token.ASSIGN_ADD) {
		
			if (tt == org.mozilla.javascript.Token.NAME) {
				System.out.println(((Name) node).getIdentifier());
			}
		} else if (tt == org.mozilla.javascript.Token.NAME
				//&& node.getLineno() == lineNo
				&& ((Name) node).getIdentifier().equals(variableName)) {
			System.out.println(((Name) node).getIdentifier());
			System.out.println(node.getLineno());
		}

		return continueToChildren;  // process kids
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

		return parse(code);
	}

	@Override
	public AstNode createPointNode(String shouldLog, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AstRoot finish(AstRoot node) {
		this.lastScopeVisited = null;
		return node;
	}

	@Override
	public void start(String node) {
		src = node;
	}

	public void start() {
	}
}
