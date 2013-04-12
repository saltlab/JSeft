package domMutation;

public class Node {
	public String selector;
	public String id;
	public String tagName;
	public String className;
	public String xpath;
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Node){
			Node node=(Node) obj;
			if(this.selector.equals(node.selector) &&this.id.equals(node.id)
					&& this.tagName.equals(node.tagName) && this.className.equals(node.className)
					&& this.xpath.equals(node.xpath)){
				return true;
			}
		}
		return false;
	}

}
