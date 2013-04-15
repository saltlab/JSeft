package domGeneration;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Element {
	
	String tagName;
	List<Attribute> attributes;
	String htmlCode;
	
/*	public Element(String tagName){
		
		this.tagName=tagName;
		attributes=new ArrayList<Attribute>();
		htmlCode="";
		
		
	}
*/	
	/* tr[2], id::3,class::name...*/
	public Element(String elementXpathString){
		String[] attrs=elementXpathString.split(",");
		tagName=attrs[0].replaceAll("\\[.*\\]", "");
		for(int i=1;i<attrs.length;i++){
			String attrName=attrs[i].split("::")[0];
			String attrVal=attrs[i].split("::")[1];
			Attribute attribute=new Attribute(attrName, attrVal);
			attributes.add(attribute);
		}
		setHtmlCode();
	}
	
	public void addAttribute(Attribute attribute){
		attributes.add(attribute);
	}
	
	public List<Attribute> getAttributes(){
		return attributes;
	}
	public String getTagName(){
		return tagName;
	}
	public void setHtmlCode(){
		String html="<" + tagName;
		for(Attribute attr:attributes){
			String attrName=attr.getAttrName();
			String attrValue=attr.getAttrValue();
			html+= attrName + "=" + "\"" + attrValue + "\"" + " ";
		}
		html+="/>";
		htmlCode=html;
		
	}
	
	public String getHtmlCode(){
		return htmlCode;
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj instanceof Element){
			Element elem=(Element) obj;
			if(this.attributes.equals(elem.getAttributes()) && this.tagName.equals(elem.getTagName())){
				return true;
			}
		}
		return false;
	}
	
	
	

}
