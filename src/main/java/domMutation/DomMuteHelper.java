package domMutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import com.crawljax.path.DOMElement;
import com.crawljax.path.Globals;

import executionTracer.DOMMutAstInstrumenter;

public class DomMuteHelper {
	
	private DomTraceReading domTraceReading;
	//excluded elements from the mutation list: because they are among our selected clickables for crawljax
	ArrayList<String> excludedElementsList=new ArrayList<String>();
	
	public DomMuteHelper(String outputFolder){
		
		domTraceReading=new DomTraceReading(outputFolder);
		Queue<DOMElement> eventSequence=Globals.allPath.getEventSequence();
		
		for(DOMElement event:eventSequence){
			HashMap<String,String> events=event.getElementAttributes();
			Set<String> keys=events.keySet();
			Iterator<String> iter=keys.iterator();
			while(iter.hasNext()){
				String attrName=iter.next();
				String attrValue=events.get(attrName);
				if(attrName.equals("xpath")){
					excludedElementsList.add(attrValue);
				}
			}
		}
		
	
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
				//excluding elements that are part of our crawljax event sequence from the list of dom mutations
				if(excludedElementsList.contains(nodeProp.getNode().xpath))
					continue;
				if(nodeProp.getTypeOfAccess().equals("DIRECTACCESS")){
					domMutAstInstrumenterList.add(new DOMMutAstInstrumenter(funcName, nodeProp, true, stateName));
				}
				else{
				
					domMutAstInstrumenterList.add(new DOMMutAstInstrumenter(funcName, nodeProp, true, stateName));
					domMutAstInstrumenterList.add(new DOMMutAstInstrumenter(funcName, nodeProp, false, stateName));
				}
			}
		}
		return domMutAstInstrumenterList;
	}

}
