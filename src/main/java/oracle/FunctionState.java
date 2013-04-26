package oracle;

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
