package domMutation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import executionTracer.DOMMutAstInstrumenter;

public class DomMuteHelper {
	
	private DomTraceReading domTraceReading;
	
	
	public DomMuteHelper(String outputFolder){
		
		domTraceReading=new DomTraceReading(outputFolder);
		
	
	}
	
	public ArrayList<DOMMutAstInstrumenter> domMutAstInstrumenterGenerator(){
		
		ArrayList<DOMMutAstInstrumenter> domMutAstInstrumenterList=new ArrayList<DOMMutAstInstrumenter>();
		TreeMap<String,ArrayList<NodeProperty>> func_domNode_map=domTraceReading.getFunc_domNode_map();
		Set<String> keys=func_domNode_map.keySet();
		Iterator<String> it=keys.iterator();
		
		while(it.hasNext()){
			String stateNameFuncName=it.next();
			ArrayList<NodeProperty> nodeProps=func_domNode_map.get(stateNameFuncName);
			String[] str=stateNameFuncName.split("-");
			String stateName=str[0];
			String funcName=str[1];
			for(int i=0;i<nodeProps.size();i++){
				
				NodeProperty nodeProp=nodeProps.get(i);
				domMutAstInstrumenterList.add(new DOMMutAstInstrumenter(funcName, nodeProp, true, stateName));
				domMutAstInstrumenterList.add(new DOMMutAstInstrumenter(funcName, nodeProp, false, stateName));
			
			}
		}
		return domMutAstInstrumenterList;
	}

}
