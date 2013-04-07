package qunitGenerator;

import oracle.FunctionPoint;
import oracle.Variable;



public class QunitAssertion {
	
	public static enum AssertionType {ok, equal, deepEqual};
	private AssertionType assertionType;
	private String assertionMsg="";
	private String expected;
	private String actual; 
	private String assertionCode;

	private Variable expectedVar;
	private FunctionPoint entryPoint;
	private FunctionPoint exitPoint;
	
	public QunitAssertion(String actual, String expected, AssertionType assertionType, Variable expectedVar, FunctionPoint entryPoint,
		FunctionPoint exitPoint){
		this.expected=expected;
		this.actual=actual;
		this.assertionType=assertionType;
		this.expectedVar=expectedVar;
		this.entryPoint=entryPoint;
		this.exitPoint=exitPoint;
		if(assertionType.name().equals(AssertionType.ok)){
			
			assertionCode=assertionType.toString() + "(" + actual +" == " + expected + ", " + "" +")" + ";";
		}
		else{
			
			assertionCode=assertionType.toString() + "(" + actual +", " + expected + ", " + "" +")" + ";";
		}
	}

	
	public AssertionType getAssertionType(){
		return assertionType;
	}
	
	public String getAssertionMsg(){
		return assertionMsg;
	}
	
	public String getExpected(){
		return expected;
	}
	
	public Variable getExpectedVar(){
		return expectedVar;
	}
	
	public FunctionPoint getEntryPoint(){
		return entryPoint;
	}
	
	public FunctionPoint getExitPoint(){
		return exitPoint;
	}
	
	public String getActual(){
		return actual;
	}
	
	
	
	public String getAssertionCode(){
		return assertionCode;
	}
}

