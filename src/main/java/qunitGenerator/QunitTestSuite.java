package qunitGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.ast.AstNode;

import oracle.FunctionPoint;
import oracle.Oracle;

import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.io.Resources;

import executionTracer.DOMAstInstrumenter;

public class QunitTestSuite {
	
	private List<QunitTestCase> qunitTestCases=new ArrayList<QunitTestCase>();
	private String testSuiteCode=getRequiredJsScripts() + "\n\n";

	/*oracle: (funcName->(entrypoint->oracle))*/
	public QunitTestSuite(ArrayListMultimap<String,ArrayListMultimap<FunctionPoint,Oracle>> oracleMultimap){
		Set<String> keys=oracleMultimap.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			String funcName=iter.next();
			List<ArrayListMultimap<FunctionPoint, Oracle>> funcEntryOracleList=oracleMultimap.get(funcName);
			for(ArrayListMultimap<FunctionPoint , Oracle> funcEntryOracle:funcEntryOracleList){
				Set<FunctionPoint> funcEntries=funcEntryOracle.keySet();
				Iterator<FunctionPoint> funcEntryIter=funcEntries.iterator();
				while(funcEntryIter.hasNext()){
					FunctionPoint funcEntry=funcEntryIter.next();
					List<Oracle> funcOracles=funcEntryOracle.get(funcEntry);
					QunitTestCase qunitTestCase=new QunitTestCase(funcOracles, funcEntry, funcName);
					qunitTestCases.add(qunitTestCase);
				}
			}
		
		}
		
		for(QunitTestCase testCase:qunitTestCases){
			testSuiteCode+=testCase.getTestCaseCode() +"\n\n";
		}
	}
	
	public List<QunitTestCase> getQunitTestCases(){
		return qunitTestCases;
	}
	
	public String getTestSuiteCode(){
		return testSuiteCode;
	}
	
	private String getRequiredJsScripts() {
		String code=null;
		
		try {
			
			code=Resources.toString(QunitTestSuite.class.getResource("/addVar_domNodePropsAccrossTheXpath.js"), Charsets.UTF_8);
		} catch (IOException e) {
	
			e.printStackTrace();
		}

	/*	File js = new File(this.getClass().getResource("/addVar.js").getFile());
		code = Helper.getContent(js);
	*/	return code;
	}
}
