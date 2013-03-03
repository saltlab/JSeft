package oracle;

public class Variable {
	
	private String variableVame;
	private String value;
	private String type;
	private String variableUsage;
	
	public Variable(String variableName, String value, String type, String variableUsage){
		this.variableVame=variableName;
		this.value=value;
		this.type=type;
		this.variableUsage=variableUsage;
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
	public String getVariableUsage(){
		return variableUsage;
	}
}


