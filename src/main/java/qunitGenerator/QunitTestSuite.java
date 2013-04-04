package qunitGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oracle.FunctionPoint;

import com.google.common.collect.ArrayListMultimap;

public class QunitTestSuite {
	
	private List<QunitTestCase> qunitTestCases=new ArrayList<QunitTestCase>();

	/*oracle: (funcName->(entrypoint->exitpoint))*/
	public QunitTestSuite(ArrayListMultimap<String,ArrayListMultimap<FunctionPoint,FunctionPoint>> oracleMultimap){
		Set<String> keys=oracleMultimap.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			String funcName=iter.next();
			List<ArrayListMultimap<FunctionPoint, FunctionPoint>> funcEntryExitList=oracleMultimap.get(funcName);
			for(ArrayListMultimap<FunctionPoint , FunctionPoint> funcEntryExit:funcEntryExitList){
				Set<FunctionPoint> funcEntries=funcEntryExit.keySet();
				Iterator<FunctionPoint> funcEntryIter=funcEntries.iterator();
				while(funcEntryIter.hasNext()){
					FunctionPoint funcEntry=funcEntryIter.next();
					List<FunctionPoint> funcExits=funcEntryExit.get(funcEntry);
					QunitTestCase qunitTestCase=new QunitTestCase(funcEntry, funcExits);
					qunitTestCases.add(qunitTestCase);
				}
			}
		
		}
	}
	
	public List<QunitTestCase> getQunitTestCases(){
		return qunitTestCases;
	}
}
