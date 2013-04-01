package domMutation;

public class DomAttribute {

	private String attributeName;
	private String attributeValue;
	
	public DomAttribute(String attributeName, String attributeValue){
		this.attributeName=attributeName;
		this.attributeValue=attributeValue;
	}
	
	public String getAttributeName(){
		return attributeName;
	}
	public String getAttributeValue(){
		return attributeValue;
	}
	
}
