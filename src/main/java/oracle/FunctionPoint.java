package oracle;

import java.util.ArrayList;

import domMutation.NodeProperty;

public class FunctionPoint {

	private String pointName;
	private ArrayList<Variable> variables;
	private String domHtml;
//	accessedDomNodes=new ArrayList<AccessedDOMNode>();
	private long time;
	
	public FunctionPoint(String pointName, ArrayList<Variable> variables, String domHtml, long time){
		this.pointName=pointName;
		this.variables=variables;
		this.time=time;
		this.domHtml=domHtml;
	}
	
	
/*	public FunctionPoint(String pointName, AccessedDOMNode accessDomNode, long time){
		this.pointName=pointName;
		this.variables=new ArrayList<Variable>();
		this.time=time;
		accessedDomNodes=new ArrayList<AccessedDOMNode>();
		accessedDomNodes.add(accessDomNode);
	}
	public void addAccessedDomNode(AccessedDOMNode domNode){
	
		accessedDomNodes.add(domNode);
	}
	
	public void addVariable(ArrayList<Variable> varList){
		
		variables.addAll(varList);
	}
	public ArrayList<AccessedDOMNode> getAccessedDomNodes(){
		return accessedDomNodes;
	}
*/	
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
	
	@Override
	public boolean equals(Object funcPoint){
		if(funcPoint instanceof FunctionPoint){
			FunctionPoint functionPoint=(FunctionPoint) funcPoint;
			if(this.getPointName().equals(functionPoint.getPointName())
					&& this.getTime()==functionPoint.getTime()
					&& this.getVariables().equals(functionPoint.getVariables())
//					&& this.getAccessedDomNodes().equals(functionPoint.getAccessedDomNodes())
					&& this.domHtml.equals(functionPoint.getDomHtml())){
				return true;
				
			}
		}
		return false;
	}
	
}
