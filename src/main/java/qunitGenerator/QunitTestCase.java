package qunitGenerator;

import java.util.ArrayList;
import java.util.List;

import oracle.FunctionPoint;

public class QunitTestCase {
	
	private String testCaseCode;
	private String testCaseName;
	private List<QunitAssertion> qunitAssertions=new ArrayList<QunitAssertion>();
	
	public QunitTestCase(FunctionPoint functionEntry, List<FunctionPoint> functionExits){
		
		for(FunctionPoint funcExit:functionExits){
			QunitAssertion qunitAssertion=new QunitAssertion(functionEntry,funcExit);
			qunitAssertions.add(qunitAssertion);
		}
		
	}
	
	public String getTestCaseCode(){
		return testCaseCode;
	}
	
	public String getTestCaseName(){
		return testCaseName;
	}
	
	public List<QunitAssertion> getQunitAssertions(){
		return qunitAssertions;
	}
	

}
