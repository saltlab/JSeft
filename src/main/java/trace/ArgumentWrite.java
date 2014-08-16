package trace;

public class ArgumentWrite extends VariableWrite {
	private int argumentNumber;
	private String functionName;

	public int getArgumentNumber() {
		return argumentNumber;
	}

	public void setArgumentNumber(int o) {
		argumentNumber = o;
	}
	
	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String f) {
		functionName = f;
	}
}
