package domMutation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import com.crawljax.path.AllPath;
import com.crawljax.path.DOMElement;
import com.crawljax.path.Globals;
import com.crawljax.util.Helper;

import executionTracer.DOMMutAstInstrumenter;

public class DomMuteHelper {
	
	private DomTraceReading domTraceReading;
	//excluded elements from the mutation list: because they are among our selected clickables for crawljax
	ArrayList<String> excludedElementsList=new ArrayList<String>();
	AllPath paths;
	public DomMuteHelper(String outputFolder){
		String filenameAndPath =  Helper.addFolderSlashIfNeeded(outputFolder) + "allPossiblePath" + ".txt";
		ArrayList<AllPath> allPath=readAllPossiblePathFile(filenameAndPath);
		for(int i=0;i<allPath.size();i++){
			paths=allPath.get(0);
		}
		
		domTraceReading=new DomTraceReading(outputFolder);
		Queue<DOMElement> eventSequence=paths.getEventSequence();
		
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
	
	
	private static ArrayList<AllPath> readAllPossiblePathFile(String filenameAndPath){
		ArrayList<AllPath> allPossiblePath=new ArrayList<AllPath>();
		try {
			BufferedReader input =
					new BufferedReader(new FileReader(filenameAndPath));
			String line="";
			while((line = input.readLine()) != null){
				
				String[] str=line.split("::");
				String startVertex=str[0];
				String endVertex=str[1];
				AllPath allPath=new AllPath(startVertex,endVertex);
				while(true){
					String attributeName="";
					String attributeValue="";
					String elementName="";
				//	ArrayList<ElementAttribute> attributes=new ArrayList<ElementAttribute>();
					DOMElement domElement = new DOMElement();
					while(!(line=input.readLine()).equals("---------------------------------------------------------------------------")){
						if(line.equals("===========================================================================")){
							allPossiblePath.add(allPath);
							break;
						}
						if(line.contains("tagName::")){
							elementName=line.split("::")[1];
							domElement.setDOMElementName(elementName);
						}
						else{
							String[] attr=line.split("::");
							attributeName=attr[0];
							attributeValue=attr[1];
				//			ElementAttribute attribute=new ElementAttribute(attributeName, attributeValue);
							domElement.setAttributes(attributeName, attributeValue);
				//			attributes.add(attribute);
						}
					}
					
					if(line.equals("===========================================================================")){
			
						break;
					}
			//		DOMElement domElement=new DOMElement(elementName, attributes);
					allPath.pushToQueue(domElement);
				
				}
			
				
			}
			input.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return allPossiblePath;
	}
	

}
