package oracle;

import domMutation.Node;
import domMutation.NodeProperty;

@Deprecated
public class AccessedDOMNode extends NodeProperty {

	private long time;
	public AccessedDOMNode(Node node, String line, String value, long time){
		super(node, line, value);
		this.time=time;
		
	}
	public long getTime(){
		return time;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof AccessedDOMNode){
			AccessedDOMNode accessedNode=(AccessedDOMNode) obj;
			if(this.getNode().equals(accessedNode.getNode()) && this.getLine().equals(accessedNode.getLine())
					&& this.getValue().equals(accessedNode.getValue())){
				return true;
			}
		}
		return false;
		
	}
}
