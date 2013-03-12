package oracle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class FunctionStateComparator {

	
	private Multimap<String, FunctionState> funcNameToFuncStateMap_modifiedVer;
	private HashMap<String,ArrayListMultimap<FunctionPoint,FunctionPoint>> oracleMultimap=new HashMap<String, ArrayListMultimap<FunctionPoint,FunctionPoint>>();	
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
				FunctionPoint origFuncEntry = null;
				ArrayList<FunctionPoint> origFuncExits=getFunctinExitsMatchedWithEntryPoint(funcEntryToMultiExit, modifiedFuncEntry, origFuncEntry);
				boolean same=false;
				for(int j=0;j<origFuncExits.size();j++){
					FunctionPoint origFuncExit=origFuncExits.get(j);
					same=functionPointsSimilar(modifiedFuncExit, origFuncExit);
					if(!same){
						
						ArrayListMultimap<FunctionPoint,FunctionPoint> funcPointMltimap=ArrayListMultimap.create();
						funcPointMltimap.put(origFuncEntry, origFuncExit);
						oracleMultimap.put(funcName, funcPointMltimap);
						break;
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
	
	private ArrayList<FunctionPoint> getFunctinExitsMatchedWithEntryPoint(Multimap<FunctionPoint, FunctionPoint> funcEntryToMultiExit, FunctionPoint funcPoint, FunctionPoint origFuncEntry){
		
		ArrayList<FunctionPoint> exitFuncPoints=new ArrayList<FunctionPoint>();
		Set<FunctionPoint> keys=funcEntryToMultiExit.keySet();
		Iterator<FunctionPoint> iter=keys.iterator();
		
		while(iter.hasNext()){
			
			FunctionPoint entryPoint=iter.next();
			if(entryPoint.getPointName().equals(funcPoint.getPointName())){
				ArrayList<Variable> origVars=entryPoint.getVariables();
				ArrayList<Variable> modifiedVars=funcPoint.getVariables();
				if(origVars.equals(modifiedVars)){
					origFuncEntry=entryPoint;
					exitFuncPoints.addAll(funcEntryToMultiExit.get(entryPoint));
					
				}
			}
		}
		
		return exitFuncPoints;
		
	}
	
	@Deprecated
	private FunctionPoint getFunctionMatchedEntryPoint(Multimap<FunctionPoint, FunctionPoint> funcEntryToMultiExit, FunctionPoint modifiedFuncPoint){
		
		
		Set<FunctionPoint> keys=funcEntryToMultiExit.keySet();
		Iterator<FunctionPoint> iter=keys.iterator();
		
		while(iter.hasNext()){
			
			FunctionPoint entryPoint=iter.next();
			if(entryPoint.getPointName().equals(modifiedFuncPoint.getPointName())){
				ArrayList<Variable> origVars=entryPoint.getVariables();
				ArrayList<Variable> modifiedVars=modifiedFuncPoint.getVariables();
				if(origVars.equals(modifiedVars)){
					
					return entryPoint;
					
				}
			}
		}
		
		return null;
		
	}
	
}
