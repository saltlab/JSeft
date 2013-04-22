package oracle;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;

import domMutation.DomAttribute;
import domMutation.Dom_Mut_Analyser;
import domMutation.Dom_Orig_Analyser;

public class DomStateComparator {

	/* (clickedOn->(elementXpath->attibutes)) */
	private ArrayListMultimap<String, ArrayListMultimap<String, DomAttribute>> domOracleMultimap=ArrayListMultimap.create();
	
	public ArrayListMultimap<String, ArrayListMultimap<String, DomAttribute>> getDomOracleMultimap(){
		return domOracleMultimap;
	}
	
	public void analysingOutPutDiffs(){
		
		Set<String> keys=Dom_Mut_Analyser.stateXpathToNodeAttrsMap_MutVer.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			String clickedOn_state_xpath=iter.next();
			String element=clickedOn_state_xpath.split("_")[2];
			String clickedOn=clickedOn_state_xpath.split("_")[0];
			List<DomAttribute> mutDomAttrs=Dom_Mut_Analyser.stateXpathToNodeAttrsMap_MutVer.get(clickedOn_state_xpath);
			List<DomAttribute> origDomAttrs=Dom_Orig_Analyser.stateXpathToNodeAttrsMap.get(clickedOn_state_xpath);
			if(origDomAttrs!=null){
				for(int i=0;i<origDomAttrs.size();i++){
					boolean matched=false;
					DomAttribute origDomAttr=mutDomAttrs.get(i);
				
					for(int j=0;j<mutDomAttrs.size();j++){
						if(origDomAttr.equals(mutDomAttrs.get(j))){
							matched=true;
							break;
						}
						
					}
				
					if(!matched){
					
						addElementToDomOracleMultimap(element, origDomAttr, clickedOn);
				
					}
				}	
			}
			else{
				
				ArrayListMultimap<String, DomAttribute> newElem=ArrayListMultimap.create();
				newElem.put(element, null);	
				domOracleMultimap.put(clickedOn, newElem);
				
			}
		}
		
		
	}
	
	private void addElementToDomOracleMultimap(String element, DomAttribute origDomAttr, String clickedOn){
		List<ArrayListMultimap<String, DomAttribute>> elemList=domOracleMultimap.get(clickedOn);
		if(elemList!=null){
			for(ArrayListMultimap<String, DomAttribute> elem:elemList){
				
				if(elem.get(element)!=null){
					
					elem.put(element, origDomAttr);
					
				}
				
				else{
					
					ArrayListMultimap<String, DomAttribute> newElem=ArrayListMultimap.create();
					newElem.put(element, origDomAttr);	
					domOracleMultimap.put(clickedOn, newElem);
					
				}
			}
		}
		else{
			ArrayListMultimap<String, DomAttribute> newElem=ArrayListMultimap.create();
			newElem.put(element, origDomAttr);	
			domOracleMultimap.put(clickedOn, newElem);
		}
		
	}
	
}
