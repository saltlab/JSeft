package oracle;

public class Variable {
	
	private String variableVame;
	private String value;
	private String type;
	
	public Variable(String variableName, String value, String type){
		this.variableVame=variableName;
		this.value=value;
		this.type=type;
	}
	
	public String getVariableName(){
		return variableVame;
	}

	public String getValue(){
		return value;
	}
	public String getType(){
		return type;
	}
}

