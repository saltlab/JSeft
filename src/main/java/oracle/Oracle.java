package oracle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Oracle {
	
	private Set<Variable> variables;
	private Set<AccessedDOMNode> accessedDomNodes;
	private FunctionPoint origVersionExitFuncPoint;
	
	public Oracle(){
		variables=new HashSet<Variable>();
		accessedDomNodes=new HashSet<AccessedDOMNode>();
	}
	
	public void addVariable(Variable var){
		variables.add(var);
	}
	
	public void addVariableList(ArrayList<Variable> varList){
		variables.addAll(varList);
	}
	
	public void addAccessedDomNode(AccessedDOMNode domNode){
		accessedDomNodes.add(domNode);
	}
	
	public void addAccessedDomNodeList(ArrayList<AccessedDOMNode> domNodeList){
		accessedDomNodes.addAll(domNodeList);
	}
	
	public Set<Variable> getVariables(){
		return variables;
	}
	
	public Set<AccessedDOMNode> getAccessedDomNodes(){
		return accessedDomNodes;
	}
	
	public void setOrigVersionExitFuncPoint(FunctionPoint origexit){
		origVersionExitFuncPoint=origexit;
		
	}
	
	public FunctionPoint getOrigVersionExitFuncPoint(){
		return origVersionExitFuncPoint;
	}
	public void addVariableSet(Set<Variable> varSet){
		variables.addAll(varSet);
	}
	public void addAccessedDomNodeSet(Set<AccessedDOMNode> accessedDomNodeSet){
		accessedDomNodes.addAll(accessedDomNodeSet);
	}
	

}
