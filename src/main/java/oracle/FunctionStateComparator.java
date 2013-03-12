package oracle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Multimap;

public class FunctionStateComparator {

	
	private Multimap<String, FunctionState> funcNameToFuncStateMap_modifiedVer;
	
	public FunctionStateComparator(Multimap<String, FunctionState> funcNameToFuncStateMap_modifiedVer){
		this.funcNameToFuncStateMap_modifiedVer=funcNameToFuncStateMap_modifiedVer;
		
	}
	
	public Multimap<String, FunctionState> getFuncNameToFuncStateMap_modifiedVer(){
		return funcNameToFuncStateMap_modifiedVer;
	}
	
	public void analysingOutputDiffs(){
		
		Set<String> keys=funcNameToFuncStateMap_modifiedVer.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			String funcName=iter.next();
			List<FunctionState> funcStates=(List<FunctionState>) funcNameToFuncStateMap_modifiedVer.get(funcName);
			Multimap<FunctionPoint, FunctionPoint> funcEntryToMultiExit=
					OriginalJsExecTraceAnalyser.funcEntryPointToExitPointMap.get(funcName);
			
			for(int i=0;i<funcStates.size();i++){
				FunctionState modifiedFuncState=funcStates.get(i);
				FunctionPoint modifiedFuncEntry=modifiedFuncState.getFunctionEntry();
				FunctionPoint modifiedFuncExit=modifiedFuncState.getFunctionExit();
				ArrayList<FunctionPoint> origFuncExits=new ArrayList<FunctionPoint>( funcEntryToMultiExit.get(modifiedFuncEntry));
				for(int j=0;j<origFuncExits.size();j++){
					FunctionPoint origFuncExit=origFuncExits.get(j);
					boolean same=functionPointsSimilar(modifiedFuncExit, origFuncExit);
					if(!same){
						
					}
				}
				
				
			}
			
		}
		
	}
	
	private boolean functionPointsSimilar(FunctionPoint funcPoint1, FunctionPoint funcPoint2){
		if(funcPoint1.getPointName().equals(funcPoint2.getPointName())){
			ArrayList<Variable> varList1=funcPoint1.getVariables();
			ArrayList<Variable> varList2=funcPoint2.getVariables();
			if(varList1.equals(varList2))
				return true;
		}
		return false;
	}
	
}
