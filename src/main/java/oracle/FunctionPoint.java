package oracle;

import java.util.ArrayList;

import domMutation.NodeProperty;

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
	
	@Override
	public boolean equals(Object funcPoint){
		if(funcPoint instanceof FunctionPoint){
			FunctionPoint functionPoint=(FunctionPoint) funcPoint;
			if(this.getPointName().equals(functionPoint.getPointName())
					&& this.getTime()==functionPoint.getTime()
					&& this.getVariables().equals(functionPoint.getVariables())){
				return true;
				
			}
		}
		return false;
	}
	
}
