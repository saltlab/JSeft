package qunitGenerator;

import java.util.ArrayList;
import java.util.List;

import executionTracer.AstInstrumenter.variableUsageType;

import oracle.FunctionPoint;
import oracle.Variable;
import qunitGenerator.QunitAssertion.AssertionType;

public class QunitTestCase {
	
	private String testCaseCode="";
	private String testCaseName="";
	private String functionName="";
//	private FunctionPoint functionEntry;
//	private List<FunctionPoint> functionExits;
	private List<QunitAssertion> qunitAssertions=new ArrayList<QunitAssertion>();
	
	public QunitTestCase(FunctionPoint functionEntry, List<FunctionPoint> functionExits, String funcName){
		if(!functionName.contains("anonymous")){
			String[] funcAndScope=funcName.split(".");
			functionName=funcAndScope[funcAndScope.length-1];
			testCaseName="\"" + "Testing " + this.functionName  + "\"";
			
			ArrayList<Variable> entryVars=functionEntry.getVariables();
			for(Variable entryVar:entryVars){
				if(entryVar.getVariableUsage().equals(variableUsageType.global.toString())){
					testCaseCode+=entryVar.getVariableName() + "= " + entryVar.getValue() + ";" + "\n";
				}
			}
			testCaseCode+="var result= ";
			
			
			testCaseCode+="function" + "(";
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
			
			
			for(FunctionPoint funcExit:functionExits){
				
				
				ArrayList<Variable> exitVars=funcExit.getVariables();
				for(Variable exitVar:exitVars){
					String varUsage=exitVar.getVariableUsage();
					
					
					if(varUsage.equals(variableUsageType.returnVal.toString())){
						if(exitVar.getValue().equals("[]")){
							QunitAssertion qunitAssertionForValueChecking=new QunitAssertion();
							qunitAssertionForValueChecking.makeQunitAssertionForVariable("result.length","0", AssertionType.ok);
							qunitAssertions.add(qunitAssertionForValueChecking);
						}
						else{
							QunitAssertion qunitAssertionForValueChecking=new QunitAssertion();
							qunitAssertionForValueChecking.makeQunitAssertionForVariable("result",exitVar.getValue(), AssertionType.deepEqual);
							qunitAssertions.add(qunitAssertionForValueChecking);
						}
						
						String actualType="getType(result)" + " == " + exitVar.getType(); 
						QunitAssertion qunitAssertionForTypeChecking=new QunitAssertion();
						qunitAssertionForTypeChecking.makeQunitAssertionForVariable(actualType,exitVar.getType(), AssertionType.ok);
						qunitAssertions.add(qunitAssertionForTypeChecking);
					}
					else{
						
						
						if(varUsage.equals(variableUsageType.global.toString())){
							
							String actual=exitVar.getVariableName();
							if(exitVar.getValue().equals("[]")){
								actual+= ".length";
								QunitAssertion qunitAssertionForValueChecking=new QunitAssertion();
								qunitAssertionForValueChecking.makeQunitAssertionForVariable(actual,"0", AssertionType.ok);
								qunitAssertions.add(qunitAssertionForValueChecking);
							}
							else{
								
								QunitAssertion qunitAssertionForValueChecking=new QunitAssertion();
								qunitAssertionForValueChecking.makeQunitAssertionForVariable(actual,exitVar.getValue(), AssertionType.deepEqual);
								qunitAssertions.add(qunitAssertionForValueChecking);
							}
							
							String actualType="getType"+ "(" + actual + ")" + " == " + exitVar.getType(); 
							QunitAssertion qunitAssertionForTypeChecking=new QunitAssertion();
							qunitAssertionForTypeChecking.makeQunitAssertionForVariable(actualType,exitVar.getType(), AssertionType.ok);
							qunitAssertions.add(qunitAssertionForTypeChecking);
						}
						
					}
				}
				
			}
			
			for(QunitAssertion qunitAssertion:qunitAssertions){
				String assertionCode=qunitAssertion.getAssertionCodeForVariable();
				testCaseCode+=assertionCode;
				testCaseCode+="\n";
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
