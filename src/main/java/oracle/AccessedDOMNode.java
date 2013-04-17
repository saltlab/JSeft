package oracle;

import domMutation.Node;
import domMutation.NodeProperty;


public class AccessedDOMNode extends Node {

	public String attributes;

	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof AccessedDOMNode){
			AccessedDOMNode accessedNode=(AccessedDOMNode) obj;
			if(this.attributes.equals(accessedNode.attributes) && this.className.equals(accessedNode.className)
					&& this.id.equals(accessedNode.id) && this.tagName.equals(accessedNode.tagName)){
				return true;
			}
		}
		return false;
		
	}
}
