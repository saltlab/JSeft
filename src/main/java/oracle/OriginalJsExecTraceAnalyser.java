package oracle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class OriginalJsExecTraceAnalyser extends JsExecTraceAnalyser{
	
	protected static HashMap<String, Multimap<FunctionPoint,FunctionPoint>> funcEntryPointToExitPointMap=new HashMap<String, Multimap<FunctionPoint,FunctionPoint>>();
	
	public OriginalJsExecTraceAnalyser(String outputFolder){
		super(outputFolder);
		createFuncEntryToFuncExitMap();
	}
	

	
	private void createFuncEntryToFuncExitMap(){
		
		Set<String> keys=funcNameToFuncStateMap.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			String funcName=iter.next();
			ArrayList<FunctionState> funcStates=new ArrayList<FunctionState>(funcNameToFuncStateMap.get(funcName));
		//	ArrayList<FunctionState> funcStates=(ArrayList<FunctionState>) ((ArrayList<FunctionState>) funcStatestemp).clone();
			
			Multimap<FunctionPoint,FunctionPoint> funcPointMltimap=ArrayListMultimap.create();
			Iterator<FunctionState> fStOuterIter=funcStates.iterator();
			while(fStOuterIter.hasNext()){
				
				FunctionState funcState=fStOuterIter.next();
				FunctionPoint funcEntry=funcState.getFunctionEntry();
				FunctionPoint funcExit=funcState.getFunctionExit();
				ArrayList<Variable> varList=funcEntry.getVariables();
				funcPointMltimap.put(funcEntry, funcExit);
				funcStates.remove(funcState);
				Iterator<FunctionState> fStIter=funcStates.iterator();
				while(fStIter.hasNext()){
					FunctionState fState=fStIter.next();
					FunctionPoint nextFuncEntry=fState.getFunctionEntry();
					ArrayList<Variable> nextVarList=nextFuncEntry.getVariables();
					
					if(nextVarList.equals(varList)){
						FunctionState nextFuncState=fState;
						FunctionPoint nextFuncExit=nextFuncState.getFunctionExit();
						List<FunctionPoint> exitPointList=(List<FunctionPoint>) funcPointMltimap.get(funcEntry);
						boolean similar=false;
						for(int count=0;count<exitPointList.size();count++){
							if(functionPointsSimilar(exitPointList.get(count),nextFuncExit)){
								similar=true;
								break;
							
							}
						}
						if(!similar){
							
							funcPointMltimap.put(funcEntry, nextFuncExit);
							
						}
						funcStates.remove(fState);
						fStIter=funcStates.iterator();
						
					}
					
		/*			if(nextVarList.size()==varList.size()){
						for(int k=0;k<nextVarList.size();k++){
							if(!nextVarList.get(k).equals(varList.get(k))){
								equal=false;
								break;
							}
						}
					}
		*/			
					
				}
				fStOuterIter=funcStates.iterator();
				
			}
			funcEntryPointToExitPointMap.put(funcName, funcPointMltimap);
			
		}
	}

}
