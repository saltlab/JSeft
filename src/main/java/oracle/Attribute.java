package oracle;

public class Attribute {

	private String attrName;
	private String attrValue;
	
	public Attribute(String attrName, String attrValue){
		this.attrName=attrName;
		this.attrValue=attrValue;
	}
	public void setAttrName(String attrName){
		this.attrName=attrName;
	}
	public void setAttrValue(String attrValue){
		this.attrValue=attrValue;
	}
	
	public String getAttrName(){
		return attrName;
	}
	public String getAttrValue(){
		return attrValue;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Attribute){
			Attribute attr=(Attribute) obj;
			if(this.attrName.equals(attr.getAttrName()) && this.getAttrValue().equals(attr.getAttrValue())){
				return true;
			}
		}
		return false;
	}
	
}
