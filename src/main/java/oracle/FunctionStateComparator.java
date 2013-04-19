package oracle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class FunctionStateComparator {

	
//	private Multimap<String, FunctionState> funcNameToFuncStateMap_modifiedVer;
	/* (funcName->(entrypoint->exitpoint)) */
	private ArrayListMultimap<String,ArrayListMultimap<FunctionPoint,FunctionPoint>> oracleMultimap=ArrayListMultimap.create();	
/*	public FunctionStateComparator(Multimap<String, FunctionState> funcNameToFuncStateMap_modifiedVer){
		this.funcNameToFuncStateMap_modifiedVer=funcNameToFuncStateMap_modifiedVer;
		
	}
	
	public Multimap<String, FunctionState> getFuncNameToFuncStateMap_modifiedVer(){
		return funcNameToFuncStateMap_modifiedVer;
	}
*/	
	public void analysingOutputDiffs(){
		
		Set<String> keys=MutatedJsExecTraceAnalyser.funcNameToFuncStateMap_modifiedVer.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
		
			String funcName=iter.next();
			List<FunctionState> funcStates=(List<FunctionState>) MutatedJsExecTraceAnalyser.funcNameToFuncStateMap_modifiedVer.get(funcName);
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
					if (same)
						break;
		/*			if(!same){
						
						ArrayListMultimap<FunctionPoint,FunctionPoint> funcPointMltimap=ArrayListMultimap.create();
						funcPointMltimap.put(origFuncEntry, origFuncExit);
						oracleMultimap.put(funcName, funcPointMltimap);
						break;
					}
		*/			
				}

				boolean isFuncEntryRepeatedInOracle=isEntryPointRepeatedInOracleSet(funcName,origFuncEntry);
				if(!same && !isFuncEntryRepeatedInOracle){

					ArrayListMultimap<FunctionPoint,FunctionPoint> funcPointMltimap=ArrayListMultimap.create();
					for(int j=0;j<origFuncExits.size();j++){
						
						FunctionPoint origFuncExit=origFuncExits.get(j);
						funcPointMltimap.put(origFuncEntry, origFuncExit);
						
					}
					oracleMultimap.put(funcName, funcPointMltimap);
				}
				
				
			}
			
		}
		
	}
	
	private boolean functionPointsSimilar(FunctionPoint funcPoint1, FunctionPoint funcPoint2){
		if(funcPoint1.getPointName().equals(funcPoint2.getPointName())){
			ArrayList<Variable> varList1=funcPoint1.getVariables();
			ArrayList<Variable> varList2=funcPoint2.getVariables();
			
			if(funcPoint1.getPointName().toLowerCase().equals("entry")){
				String domHtml1=funcPoint1.getDomHtml();
				String domHtml2=funcPoint2.getDomHtml();
				if(varList1.equals(varList2) && domHtml1.equals(domHtml2))
					return true;
			}
			
			else if(funcPoint1.getPointName().toLowerCase().equals("exit")){
				ArrayList<AccessedDOMNode> accessedNodes1=funcPoint1.getAccessedDomNodes();
				ArrayList<AccessedDOMNode> accessedNodes2=funcPoint2.getAccessedDomNodes();
				if(varList1.equals(varList2) && accessedNodes1.equals(accessedNodes2)){
					return true;
				}
			}
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
/*				ArrayList<AccessedDOMNode> origNodes=entryPoint.getAccessedDomNodes();
				ArrayList<AccessedDOMNode> modifiedNodes=funcPoint.getAccessedDomNodes();
*/				
				String origDomHtml=entryPoint.getDomHtml();
				String modifiedDomHtml=funcPoint.getDomHtml();
				if(origVars.equals(modifiedVars) && origDomHtml.equals(modifiedDomHtml)){
					origFuncEntry=entryPoint;
					exitFuncPoints.addAll(funcEntryToMultiExit.get(entryPoint));
					break;
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
/*				ArrayList<AccessedDOMNode> origNodes=entryPoint.getAccessedDomNodes();
				ArrayList<AccessedDOMNode> modifiedNodes=modifiedFuncPoint.getAccessedDomNodes();
*/				
				
				String origDomHtml=entryPoint.getDomHtml();
				String modifiedDomHtml=modifiedFuncPoint.getDomHtml();
				if(origVars.equals(modifiedVars) && origDomHtml.equals(modifiedDomHtml)){
					
					return entryPoint;
					
				}
			}
		}
		
		return null;
		
	}
	
	private boolean isEntryPointRepeatedInOracleSet(String funcName, FunctionPoint origFuncEntry){
		
		List<ArrayListMultimap<FunctionPoint, FunctionPoint>> entryExitList=oracleMultimap.get(funcName);
		for(ArrayListMultimap<FunctionPoint, FunctionPoint> entryExit:entryExitList){
			
			Set<FunctionPoint> key=entryExit.keySet();
			Iterator<FunctionPoint> iterator=key.iterator();
			while(iterator.hasNext()){
				FunctionPoint entryPoint=iterator.next();
				if(entryPoint.getPointName().equals(origFuncEntry.getPointName())){
					if(entryPoint.getVariables().equals(origFuncEntry.getVariables())
							&& entryPoint.getDomHtml().equals(origFuncEntry.getDomHtml())){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
}
