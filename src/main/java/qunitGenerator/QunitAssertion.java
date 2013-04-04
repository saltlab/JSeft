package qunitGenerator;

import oracle.FunctionPoint;
import oracle.Variable;



public class QunitAssertion {
	
	public static enum AssertionType {ok, equal, deepEqual};
	private AssertionType assertionType;
	private String assertionMsg;
	private Variable expectedOutCome;
	private FunctionPoint functionEntry;
	private FunctionPoint functionExit;
	private String functionName;
	
	public QunitAssertion(FunctionPoint functionEntry, FunctionPoint functionExit){
		this.functionEntry=functionEntry;
		this.functionExit=functionExit;
	}

	
	public AssertionType getAssertionType(){
		return assertionType;
	}
	
	public String getAssertionMsg(){
		return assertionMsg;
	}
	
	public Variable getExpectedOutCome(){
		return expectedOutCome;
	}
	
	public FunctionPoint getFunctionEntry(){
		return functionEntry;
	}
	
	public FunctionPoint getFunctionExit(){
		return functionExit;
	}
	
	public String getFunctionName(){
		return functionName;
	}
}
