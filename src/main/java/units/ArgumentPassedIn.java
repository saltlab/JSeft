package units;

import org.mozilla.javascript.ast.FunctionNode;

public class ArgumentPassedIn {

	private FunctionNode fn;
	private String arg;
	private int argNum;

	public ArgumentPassedIn(FunctionNode f, String a, int i) {
		this.fn = f;
		this.arg = a;
		this.argNum = i;
	}
	
	public void setArgument(String a) {
		this.arg = a;
	}
	
	public String getArgument() {
		return this.arg;
	}
	
	public void setFunction(FunctionNode f) {
		this.fn = f;
	}
	
	public FunctionNode getFunction() {
		return this.fn;
	}
	
	public void setArgumentNumber(int f) {
		this.argNum = f;
	}
	
	public int getArgumentNumber() {
		return this.argNum;
	}
}
