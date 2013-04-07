package qunitGenerator;

import java.util.ArrayList;
import java.util.List;

import executionTracer.AstInstrumenter.variableUsageType;

import oracle.FunctionPoint;
import oracle.Variable;
import qunitGenerator.QunitAssertion.AssertionType;

public class QunitTestCase {
	
	private String testCaseCode;
	private String testCaseName;
	private String functionName;
//	private FunctionPoint functionEntry;
//	private List<FunctionPoint> functionExits;
	private List<QunitAssertion> qunitAssertions=new ArrayList<QunitAssertion>();
	
	public QunitTestCase(FunctionPoint functionEntry, List<FunctionPoint> functionExits, String funcName){
		String[] funcAndScope=funcName.split(".");
		functionName=funcAndScope[funcAndScope.length-1];
		testCaseName="\"" + "Testing " + this.functionName + "\"";

		testCaseCode="var result= ";
		ArrayList<Variable> entryVars=functionEntry.getVariables();
		if(!functionName.contains("anonymous")){
			testCaseCode="function" + "(";
			for(Variable entryVar:entryVars){
				String varUsage=entryVar.getVariableUsage();
				if(varUsage.equals(variableUsageType.inputParam.toString())){
					String varName=entryVar.getVariableName();
					testCaseCode+=varName + ", ";
				}
			}
			if(testCaseCode.endsWith(", ")){
				testCaseCode=testCaseCode.substring(0, testCaseCode.length()-2);
			}
			testCaseCode+=")" + ";";
			testCaseCode += "\n";
		}
		
		for(FunctionPoint funcExit:functionExits){
			
			
			ArrayList<Variable> exitVars=funcExit.getVariables();
			for(Variable exitVar:exitVars){
				String varUsage=exitVar.getVariableUsage();
				
				
				if(varUsage.equals(variableUsageType.returnVal.toString())){
					if(exitVar.getValue().equals("[]")){
						QunitAssertion qunitAssertionForValueChecking=new QunitAssertion("result.length","0", AssertionType.ok, exitVar, functionEntry,funcExit );
						qunitAssertions.add(qunitAssertionForValueChecking);
					}
					else{
						QunitAssertion qunitAssertionForValueChecking=new QunitAssertion("result",exitVar.getValue(), AssertionType.deepEqual, exitVar, functionEntry,funcExit);
						qunitAssertions.add(qunitAssertionForValueChecking);
					}
					
					String actualType="getType(result)" + " == " + exitVar.getType(); 
					QunitAssertion qunitAssertionForTypeChecking=new QunitAssertion(actualType,exitVar.getType(), AssertionType.ok, exitVar, functionEntry,funcExit);
					qunitAssertions.add(qunitAssertionForTypeChecking);
				}
				else{
					
					
					if(varUsage.equals(variableUsageType.global.toString())){
						
						String actual=exitVar.getVariableName();
						if(exitVar.getValue().equals("[]")){
							actual+= ".length";
							QunitAssertion qunitAssertionForValueChecking=new QunitAssertion(actual,"0", AssertionType.ok, exitVar, functionEntry,funcExit);
							qunitAssertions.add(qunitAssertionForValueChecking);
						}
						else{
							
							QunitAssertion qunitAssertionForValueChecking=new QunitAssertion(actual,exitVar.getValue(), AssertionType.deepEqual, exitVar, functionEntry,funcExit);
							qunitAssertions.add(qunitAssertionForValueChecking);
						}
						
						String actualType="getType"+ "(" + actual + ")" + " == " + exitVar.getType(); 
						QunitAssertion qunitAssertionForTypeChecking=new QunitAssertion(actualType,exitVar.getType(), AssertionType.ok, exitVar, functionEntry,funcExit);
						qunitAssertions.add(qunitAssertionForTypeChecking);
					}
					
				}
			}
			
		}
		
		for(QunitAssertion qunitAssertion:qunitAssertions){
			String assertionCode=qunitAssertion.getAssertionCode();
			testCaseCode+=assertionCode;
			testCaseCode+="\n";
		}
		
	}
	
	public String getTestCaseCode(){
		return testCaseCode;
	}
	
	public String getTestCaseName(){
		return testCaseName;
	}
	
	public String getfunctionName(){
		return functionName;
	}
	
	public List<QunitAssertion> getQunitAssertions(){
		return qunitAssertions;
	}
	

}
