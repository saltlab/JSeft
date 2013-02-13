package domMutation;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.crawljax.util.XPathHelper;

public class DOMMatchedElements {
	
	private ArrayList<NodeProperty> nodeProperties=new ArrayList<NodeProperty>();
	
	public DOMMatchedElements(String outputFolder){
		
		DomTraceReading domTraceReading=new DomTraceReading(outputFolder);
		nodeProperties=domTraceReading.getAllNodes();
		
	}
	
	public ArrayList<NodeProperty> findMatchedDomElemenet(Element element){
		ArrayList<NodeProperty> nodeProps=new ArrayList<NodeProperty>();
		String xpath=XPathHelper.getXPathExpression(element);
		for(int i=0;i<nodeProperties.size();i++){
			if(nodeProperties.get(i).getNode().xpath.equals(xpath)){
				nodeProps.add(nodeProperties.get(i));
			}
		}
		return nodeProps;
		
	}

}
