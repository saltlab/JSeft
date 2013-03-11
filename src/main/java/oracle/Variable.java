package oracle;

public class Variable {
	
	private String variableName;
	private String value;
	private String type;
	private String variableUsage;
	
	public Variable(String variableName, String value, String type, String variableUsage){
		this.variableName=variableName;
		this.value=value;
		this.type=type;
		this.variableUsage=variableUsage;
	
	}
	
	public String getVariableName(){
		return variableName;
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
	
	@Override
	public String toString(){
		String str=this.type+this.value+this.variableUsage+this.variableName;
		
		return str;
		
		
	}
	
	@Override
	public boolean equals(Object variable){
		Variable var=(Variable) variable;
		String str=var.toString();
		if(str.equals(this.toString()))
			return true;
		return false;
		
	}
}



