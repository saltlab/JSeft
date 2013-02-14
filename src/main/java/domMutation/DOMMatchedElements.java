package domMutation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.crawljax.util.XPathHelper;

public class DOMMatchedElements {
	
	private DomTraceReading domTraceReading;
	
	public DOMMatchedElements(String outputFolder){
		
		domTraceReading=new DomTraceReading(outputFolder);
		
		
	}
	
	public ArrayList<NodeProperty> findMatchedDomElemenet(Element element, String stateName){
		ArrayList<NodeProperty> nodeProps=new ArrayList<NodeProperty>();
		TreeMap<String,ArrayList<NodeProperty>> func_domNode_map=domTraceReading.getFunc_domNode_map();
		Set<String> keys=func_domNode_map.keySet();
		Iterator<String> it=keys.iterator();
		while(it.hasNext()){
			String stateNameFuncName=it.next();
			String state=stateNameFuncName.split("-")[0];
			if(state.equals(stateName)){
				ArrayList<NodeProperty> nodeProperties=func_domNode_map.get(stateNameFuncName);
				String xpath=XPathHelper.getXPathExpression(element);
				for(int i=0;i<nodeProperties.size();i++){
					if(nodeProperties.get(i).getNode().xpath.equals(xpath)){
						nodeProps.add(nodeProperties.get(i));
					}
				}
			}
		}

		return nodeProps;
		
	}

}
