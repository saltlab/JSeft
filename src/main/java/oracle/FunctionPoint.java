package oracle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

import domMutation.NodeProperty;
import executionTracer.AstInstrumenter.variableUsageType;

public class FunctionPoint {

	private String pointName;
	private ArrayList<Variable> variables;
	private String domHtml;
	ArrayList<AccessedDOMNode> accessedDomNodes=new ArrayList<AccessedDOMNode>();
	private long time;
	private FunctionBranchCoverage functionBranchCoverage;
	
	public FunctionPoint(String pointName, ArrayList<Variable> variables, String domHtml, long time, FunctionBranchCoverage funcBrnCoverage){
		this.pointName=pointName;
		this.variables=variables;
		this.time=time;
		this.domHtml=domHtml;
		this.functionBranchCoverage=funcBrnCoverage;
	}
	
	
/*	public FunctionPoint(String pointName, AccessedDOMNode accessDomNode, long time){
		this.pointName=pointName;
		this.variables=new ArrayList<Variable>();
		this.time=time;
		accessedDomNodes=new ArrayList<AccessedDOMNode>();
		accessedDomNodes.add(accessDomNode);
	}
*/	public void addAccessedDomNodes(ArrayList<AccessedDOMNode> domNodes){
	
		accessedDomNodes.addAll(domNodes);
	}
	
	public void addVariable(ArrayList<Variable> varList){
		
		variables.addAll(varList);
	}
	public ArrayList<AccessedDOMNode> getAccessedDomNodes(){
		return accessedDomNodes;
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
	public String getDomHtml(){
		return domHtml;
	}
	
	public FunctionBranchCoverage getFunctionBranchCoverage(){
		return functionBranchCoverage;
	}
	
	@Override
	public boolean equals(Object funcPoint){
		if(funcPoint instanceof FunctionPoint){
			FunctionPoint functionPoint=(FunctionPoint) funcPoint;
			if(this.getPointName().equals(functionPoint.getPointName())
					&& this.getTime()==functionPoint.getTime()
					&& this.getVariables().equals(functionPoint.getVariables())
					&& this.getAccessedDomNodes().equals(functionPoint.getAccessedDomNodes())
					&& this.domHtml.equals(functionPoint.getDomHtml())){
				return true;
				
			}
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return  new HashCodeBuilder(7, 11).
	            append(this.getPointName().toString()
						+ Long.toString(this.getTime())
						+ this.getVariables().toString()
						+ this.getAccessedDomNodes().toString()
						+ this.domHtml.toString()).
	            toHashCode();
		
	}
	
	public String getStringForm(){
		return this.getPointName().toString()
		+ this.getVariables().toString()
		+ this.getAccessedDomNodes().toString()
		+ this.domHtml.toString();
	}


	public void addGlobVariableIfNotExist(ArrayList<Variable> varList) {
		for(Variable var:varList){
			if(var.getVariableUsage().equals(variableUsageType.global.toString())){
				boolean found=false;
				for(Variable thisvar:this.variables){
					if(thisvar.getVariableName().equals(var.getVariableName()) &&
							thisvar.getVariableUsage().equals(var.getVariableUsage())){
						found=true;
						break;
						
					}
				}
				if(!found){
					this.variables.add(var);
				}
			}
		}
		
		
	}
	
	public List<Variable> getReturnedVariables(){
		
		List<Variable> retVars=new ArrayList<Variable>();
		for(Variable var:this.variables){
			if(var.getVariableUsage().equals(variableUsageType.returnVal.toString())){
				retVars.add(var);
			}
			if(this.pointName.equals("exit") && var.getVariableUsage().equals(variableUsageType.global.toString())){
				retVars.add(var);
			}
		}
		return retVars;
	}
}
