package instrument;

import java.util.ArrayList;
import java.util.Iterator;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Scope;
import instrument.helpers.FunctionCallParser;

public class FunctionDeclarationInstrumenter extends AstInstrumenter {
	private static final String ARGWRITE = "_dynoWriteArg";
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
	 * List with regular expressions of variables that should not be instrumented.
	 */
	private ArrayList<String> excludeList = new ArrayList<String>();


	/**
	 * Construct without patterns.
	 */
	public FunctionDeclarationInstrumenter() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public FunctionDeclarationInstrumenter(ArrayList<String> excludes) {
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
		Parser p = new Parser(compilerEnvirons, errorReporter);

		System.out.println("[parsing compilerEnvirons]: ");



		code = code.replaceAll("\\;\\\n\\ \\,", ",")
				.replaceAll("\"", "\'")
				.replaceAll("\\.\\[", "[")
				.replaceAll("\\;\\\n\\)", ")");

		System.out.println(code);



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

	public void setLineNo(int l) {
		this.lineNo = l;
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


		if (tt == org.mozilla.javascript.Token.FUNCTION && node.equals(targetFn)) {
			// TODO:
			handleFunctionDeclaration((FunctionNode) node);
		}

		return true;  // process kids
	}
	
	private void handleFunctionDeclaration(FunctionNode node) {
		
		ReturnStatementInstrumenter rsi = new ReturnStatementInstrumenter();
		rsi.setParentFunction(node);
		rsi.setScopeName(getScopeName());
		node.visit(rsi);
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
		
		// Store information on function declarations
		ArrayList<String> wrapperArgs = new ArrayList<String>();
        wrapperArgs.add(argumentName);
        wrapperArgs.add(argumentName);
        wrapperArgs.add("\""+name+"\"");
        wrapperArgs.add(argumentNumber+"");
        wrapperArgs.add(node.getLineno()+"");		
        wrapperArgs.add("\""+this.getScopeName()+"\"");		
		node.getBody().addChildToFront(parse(generateWrapper(ARGWRITE, wrapperArgs), node.getLineno()));
		
		// Don't need to add return tracking for each argument, if the bottom of the function was already 
		// instrumented...don't do it again!
		ArrayList<String> wrapperArgs2 = new ArrayList<String>();
		wrapperArgs2.add(name);
		wrapperArgs2.add("undefined");
		wrapperArgs2.add("\""+getScopeName()+"\"");			
		wrapperArgs2.add(node.getLineno()+"");
		
		AstNode lastChild = (AstNode) node.getBody().getLastChild();
		System.out.println(lastChild.toSource());
		System.out.println(lastChild.getClass());
		
		if (lastChild instanceof AstRoot
				&& lastChild.toSource().indexOf(READFUNCRET) == 0) {
			System.out.println("Bottom already instrumented!");
			return;
		}
		
		node.getBody().addChildToBack(parse(generateWrapper(READFUNCRET, wrapperArgs2), node.getLineno()));		
		
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

		//	System.out.println(isc);

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

	private boolean isAnArgument(FunctionCall node, String name) {

		ArrayList<AstNode> argumentNames = FunctionCallParser.getArgumentDependencies(node);
		Iterator<AstNode> argumentIterator = argumentNames.iterator();
		AstNode nextArgument;
		boolean found = false;

		while (argumentIterator.hasNext()) {
			nextArgument = argumentIterator.next();
			if (nextArgument instanceof Name && ((Name) nextArgument).getIdentifier().equals(name)) {
				found = true;
				break;
			} else if ((nextArgument instanceof KeywordLiteral) && nextArgument.toSource().equals("this") && name.equals("this")) {
				found = true;
				break;
			}
		}

		return found;
	}

	// Argument
	private String argumentName = null;
	public void setArgumentName (String name) {
		this.argumentName = name;
	}
	public String getArgumentName () {
		return this.argumentName;
	}
	
	// Number
	private int argumentNumber;
	public void setArgumentNumber (int num) {
		this.argumentNumber = num;
	}
	public int getArgumentNumber () {
		return this.argumentNumber;
	}
	
	// Function
	private FunctionNode targetFn = null;
	public void setTargetFunction (FunctionNode fn) {
		this.targetFn = fn;
	}
	public FunctionNode getTargetFunction () {
		return this.targetFn;
	}
}