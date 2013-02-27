package oracle;

import java.util.ArrayList;

public class FunctionPoint {

	private String pointName;
	private ArrayList<Variable> variables;
	private long time;
	
	public FunctionPoint(String pointName, ArrayList<Variable> variables, long time){
		this.pointName=pointName;
		this.variables=variables;
		this.time=time;
	}
	
	public String getPointName(){
		return pointName;
	}
	public ArrayList<Variable> getVariables(){
		return variables;
	}
	public long getTime(){
		return time;
	}
}
