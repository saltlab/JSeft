package oracle;

import java.util.HashSet;
import java.util.Set;

import domMutation.Node;




public class AccessedDOMNode extends Node {

	public String attributes;
	private Set<Attribute> allAttributes=new HashSet<Attribute>();
	
	
	public void addAttribute(Attribute attr){

		allAttributes.add(attr);
	}
	
	public Set<Attribute> getAllAttibutes(){
		return allAttributes;
	}
	public void makeAllAttributes(){
		String attrName="class";
		String attrValue=this.className;
		allAttributes.add(new Attribute("class", this.className));
		allAttributes.add(new Attribute("id",this.id));
		allAttributes.add(new Attribute("tagName",this.tagName));
	
		
		String[] attrs=attributes.split(",");
		for(int i=0;i<attrs.length;i++){
			attrName=attrs[i].split("::")[0];
			attrValue=attrs[i].split("::")[1];
			Attribute attr=new Attribute(attrName, attrValue);
			allAttributes.add(attr);
		}
		
	}
	
	
	
	
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
