package oracle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class FunctionState {

	private FunctionPoint functionEntry;
	private FunctionPoint functionExit;
	
	public FunctionState(FunctionPoint functionEntry,  FunctionPoint functionExit){
		this.functionEntry=functionEntry;
		this.functionExit=functionExit;
	}
	
	public FunctionPoint getFunctionEntry(){
		return functionEntry;
	}
	public FunctionPoint getFunctionExit(){
		return functionExit;
	}
	
	/**
	 * just for state abstraction
	 */
	private boolean isSameReturnType(FunctionState exitSt){
		List<Variable> varList=exitSt.getFunctionExit().getReturnedVariables();
		if(varList.size()!=functionExit.getReturnedVariables().size())
			return false;
		boolean same=false;
		for(Variable var:varList){
			same=false;
			for(Variable thisVar:functionExit.getReturnedVariables()){
				if(var.getType().equals(thisVar.getType())){
					same=true;
					break;
					
				}
			}
			if(!same){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * just for state abstraction
	 * @param fState
	 * @return
	 */
	private boolean isSameAccessedDOMNodes(FunctionState fState){
		ArrayList<AccessedDOMNode> domNodes=fState.getFunctionExit().accessedDomNodes;
		if(domNodes.size()!=this.getFunctionExit().accessedDomNodes.size())
			return false;
		boolean same=false;
		for(AccessedDOMNode node:domNodes){
			same=false;
			for(AccessedDOMNode thisNode:functionExit.accessedDomNodes){
				if(node.isSameDOMNode(thisNode)){
					same=true;
					break;
				}
			}
			if(!same){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * just for state abstraction
	 */
	public boolean similarState_DOM_RetType(FunctionState fState){
		if(this.isSameAccessedDOMNodes(fState) && this.isSameReturnType(fState))
			return true;
		return false;
	}
	
	/**
	 * just for state abstraction
	 */
	public boolean sameBranchCoverage(FunctionState fState){
		FunctionBranchCoverage fBrCovg=fState.functionExit.getFunctionBranchCoverage();
		if(this.functionExit.getFunctionBranchCoverage().isSameFunctionBrCovg(fBrCovg)){
			return true;
		}
		return false;
	}
	@Override
	public boolean equals(Object funcState){
		if(funcState instanceof FunctionState){
			FunctionState functionState=(FunctionState) funcState;
			if(this.getFunctionEntry().getStringForm().equals(functionState.getFunctionEntry().getStringForm())
					&& this.getFunctionExit().getStringForm().equals(functionState.getFunctionExit().getStringForm())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return  new HashCodeBuilder(23, 41).
	            append(this.getFunctionEntry().getStringForm() +
						this.getFunctionExit().getStringForm()).
	            toHashCode();
		
	}
}
