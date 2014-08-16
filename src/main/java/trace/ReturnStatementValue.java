package trace;

public class ReturnStatementValue extends RWOperation {
	private String functionName;
	private String value;

	public void setFunctionName(String f) {
		this.functionName = f;
	}
	
	public String  getFunctionName() {
		return this.functionName;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String o) {
		value = o;
	}

}
