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
		testCaseName="\"" + "Testing " + this.functionName;

		String actual="var result= ";
		ArrayList<Variable> entryVars=functionEntry.getVariables();
		if(!functionName.contains("anonymous")){
			actual="function" + "(";
			for(Variable entryVar:entryVars){
				String varUsage=entryVar.getVariableUsage();
				if(varUsage.equals(variableUsageType.inputParam.toString())){
					String varName=entryVar.getVariableName();
					actual+=varName + ", ";
				}
			}
			actual=actual.substring(0, actual.length()-2);
			actual+=")";
		}
		
		for(FunctionPoint funcExit:functionExits){
			
			
			ArrayList<Variable> exitVars=funcExit.getVariables();
			for(Variable exitVar:exitVars){
				String varUsage=exitVar.getVariableUsage();
				if(varUsage.equals(variableUsageType.returnVal.toString())){
					QunitAssertion qunitAssertion=new QunitAssertion(actual,exitVar, AssertionType.equal);
					qunitAssertions.add(qunitAssertion);
				}
			}
			
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
