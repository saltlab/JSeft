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
}
