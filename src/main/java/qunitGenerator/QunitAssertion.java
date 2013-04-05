package qunitGenerator;

import oracle.FunctionPoint;
import oracle.Variable;



public class QunitAssertion {
	
	public static enum AssertionType {ok, equal, deepEqual};
	private AssertionType assertionType;
	private String assertionMsg;
	private Variable expected;
	private String actual; 
	private String functionName;
	
	public QunitAssertion(String actual, Variable expected, AssertionType assertionType){
		this.expected=expected;
		this.actual=actual;
		this.assertionType=assertionType;
	}

	
	public AssertionType getAssertionType(){
		return assertionType;
	}
	
	public String getAssertionMsg(){
		return assertionMsg;
	}
	
	public Variable getExpected(){
		return expected;
	}
	
	public String getActual(){
		return actual;
	}
	
	public String getFunctionName(){
		return functionName;
	}
}
