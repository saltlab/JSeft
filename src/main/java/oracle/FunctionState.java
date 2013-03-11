package oracle;

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
			if(this.getFunctionEntry().equals(functionState.getFunctionEntry())
					&& this.getFunctionExit().equals(functionState.getFunctionExit())){
				return true;
			}
		}
		return false;
	}
}
