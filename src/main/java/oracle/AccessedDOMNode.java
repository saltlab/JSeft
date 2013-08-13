package oracle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;

import domMutation.Node;




public class AccessedDOMNode extends Node {

	public String attributes="";
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
		if(!this.className.equals(""))
			allAttributes.add(new Attribute("class", this.className));
		if(!this.id.equals(""))
			allAttributes.add(new Attribute("id",this.id));
		if(!this.tagName.equals(""))
			allAttributes.add(new Attribute("tagName",this.tagName));
	
		
		String[] attrs=attributes.split(":::");
		for(int i=0;i<attrs.length;i++){
			attrName=attrs[i].split("::")[0];
			if(attrs[i].split("::").length==1)
				continue;
			else
				attrValue=attrs[i].split("::")[1];
			Attribute attr=new Attribute(attrName, attrValue);
			allAttributes.add(attr);
		}
		
		
		
	}
	
	/**
	 *  only for state abstraction
	 */
	public boolean isDOMNodeChanged(AccessedDOMNode domNode){
		
		if(this.className.equals(domNode.className)
					&& this.id.equals(domNode.id) && this.tagName.equals(domNode.tagName) && this.allAttributes.size()==domNode.allAttributes.size()){
			Set<Attribute> attrSet=domNode.allAttributes;
			Iterator<Attribute> attrIter=attrSet.iterator();
			
			boolean sameAttr=false;
			while(attrIter.hasNext()){
				sameAttr=false;
				Attribute attr=attrIter.next();
				Iterator<Attribute> thisAttrIter=allAttributes.iterator();
				while(thisAttrIter.hasNext()){
					Attribute thisAttr=thisAttrIter.next();
					if(thisAttr.sameAttrName(attr)){
						sameAttr=true;
						break;
						
					}
				}
				if(!sameAttr){
					return false;
				}
			}
			
			return true;
		}
		
		return false;
		
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
	
	
	@Override 
	public String toString(){
		return this.attributes.toString() + this.className.toString()
				+ this.id.toString() + this.tagName.toString();
	}
	@Override
	public int hashCode(){
		return  new HashCodeBuilder(19, 37).
	            append(this.toString()).
	            toHashCode();
		
	}
}
