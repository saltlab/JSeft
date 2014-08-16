package units;

import java.util.ArrayList;

public class FunctionArgumentPair {

	private String functionName = "";
	private ArrayList<Integer> argumentsOfInterest = new ArrayList<Integer>();
	
	public FunctionArgumentPair (String name) {
		this.functionName = name;
	}
	
	public void addArgumentToWatch(int i) {
		this.argumentsOfInterest.add(i);
	}
	
	public ArrayList<Integer> getArgumentsOfInterest() {
		return this.argumentsOfInterest;
	}
	
	public void setFunctionName (String newName) {
		this.functionName = newName;
	}
	
	public String getFunctionName () {
		return this.functionName;
	}
	
	public boolean equals( FunctionArgumentPair other) {
		if (this.functionName.equals(other.getFunctionName())) {
			return true;
		}
		return false;
	}
	
}
