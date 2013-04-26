package qunitGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import executionTracer.AstInstrumenter.variableUsageType;

import oracle.AccessedDOMNode;
import oracle.FunctionPoint;
import oracle.Oracle;
import oracle.Variable;
import qunitGenerator.QunitAssertion.AssertionType;

public class QunitTestCase {
	
	private String testCaseCode="";
	private String testCaseName="";
	private String functionName="";
	private FunctionPoint functionEntryPoint;
	private List<QunitAssertion> qunitAssertions=new ArrayList<QunitAssertion>();
	List<Oracle> oracles=new ArrayList<Oracle>();
	public QunitTestCase(List<Oracle> oracleList, FunctionPoint functionEntry, String funcName){
		
		oracles=oracleList;
		if(!functionName.contains("anonymous") && oracleList.size()>0){// && oracleList.size()==1){
		
			Set<Variable> exitVars;
			Set<AccessedDOMNode> domNodes;
			if(oracles.size()==1){
				exitVars=oracles.get(0).getVariables();
				domNodes=oracles.get(0).getAccessedDomNodes();
			}
			else {
				exitVars=getCommonVariablesAmongOracleList();
				domNodes=getCommonAccessedDomAmongOracleList();
			}
			
			if((exitVars!=null && exitVars.size()!=0) || (domNodes!=null && domNodes.size()!=0)){
				oracles=oracleList;
				functionEntryPoint=functionEntry;
				String[] funcAndScope=funcName.split("\\.");
				functionName=funcAndScope[funcAndScope.length-1];
				testCaseName="\"" + "Testing " + this.functionName  + "\"";
				
				ArrayList<Variable> entryVars=functionEntry.getVariables();
				for(Variable entryVar:entryVars){
					if(entryVar.getVariableUsage().equals(variableUsageType.global.toString()) ||
							entryVar.getVariableUsage().equals(variableUsageType.inputParam.toString())){
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
				
				
				
		//		for(Oracle oracle:oracleList){
	
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
							
						String actualType="getType(result)" + " == " + "'" + exitVar.getType() + "'"; 
						QunitAssertion qunitAssertionForTypeChecking=new QunitAssertion();
						qunitAssertionForTypeChecking.makeQunitAssertionForVariable(actualType, exitVar.getType(), AssertionType.ok);
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
								
							String actualType="getType"+ "(" + actual + ")" + " == " + "'" + exitVar.getType() + "'"; 
							QunitAssertion qunitAssertionForTypeChecking=new QunitAssertion();
							qunitAssertionForTypeChecking.makeQunitAssertionForVariable(actualType,exitVar.getType(), AssertionType.ok);
							qunitAssertions.add(qunitAssertionForTypeChecking);
						}
							
					}
				}
					
				for(AccessedDOMNode domNode:domNodes){
					QunitAssertion qunitAssertionForDomChecking=new QunitAssertion();
					qunitAssertionForDomChecking.makeQunitAssertionForDomNode(domNode);
					qunitAssertions.add(qunitAssertionForDomChecking);
				}
					
					
					
		//		}
				
				int numberofExpectedAssertions=0;
				for(QunitAssertion qunitAssertion:qunitAssertions){
					String assertionCode=qunitAssertion.getAssertionCodeForVariable();
					testCaseCode+=assertionCode;
					testCaseCode+="\n";
					numberofExpectedAssertions+=qunitAssertion.getTotalNumberOfAssertions();
					
				}
				
				
				String testCodeSetup="test"+"(" + testCaseName + "," + numberofExpectedAssertions + ","
						+ "function()" +"{" +"\n";
				if(!functionEntry.getDomHtml().equals("")){
					String domHtml=functionEntry.getDomHtml();
					if(domHtml.startsWith("[\"") && domHtml.endsWith("\"]")){
						domHtml=domHtml.substring(2, domHtml.length()-2);
					}
					String qunitFixture="var $fixture = $(\"#qunit-fixture\");" + "\n";
					qunitFixture+="$fixture.append"+ "(" + "\"" + "<div>" + domHtml +"</div>" + "\"" + ")"+ ";" + "\n";
					testCodeSetup+=qunitFixture;
				}
				testCaseCode=testCodeSetup.concat(testCaseCode);
				testCaseCode+= "});";
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
	
	public List<Oracle> getOracles(){
		return oracles;
	}
	
	public FunctionPoint getFunctionEntryPoint(){
		return functionEntryPoint;
	}
	
	private Set<Variable> getCommonVariablesAmongOracleList(){
		
		Set<Variable> commonVars=oracles.get(0).getVariables();
	
		for(int i=1;i<oracles.size();i++){
			Set<Variable> varList=oracles.get(i).getVariables();
			commonVars=com.google.common.collect.Sets.intersection(commonVars, varList);
		}
		
		return commonVars;
		
	}
	
	private Set<AccessedDOMNode> getCommonAccessedDomAmongOracleList(){
	
		Set<AccessedDOMNode> commonAccessedDom=oracles.get(0).getAccessedDomNodes();
	
		for(int i=1;i<oracles.size();i++){
			Set<AccessedDOMNode> accessedDomList=oracles.get(i).getAccessedDomNodes();
			commonAccessedDom=com.google.common.collect.Sets.intersection(commonAccessedDom, accessedDomList);
		}
		
		return commonAccessedDom;
		
	}
	
	

}
