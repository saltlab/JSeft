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
	@Override
	public boolean equals(Object domAttribute){
		if(domAttribute instanceof DomAttribute){
			DomAttribute attribute=(DomAttribute)domAttribute;
			if(attribute.getAttributeName().equals(attributeName)
					&& attribute.getAttributeValue().equals(attributeValue))
				return true;
		}
		return false;
	}
	
	
	
}
