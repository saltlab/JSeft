package qunitGenerator;

import java.util.Iterator;
import java.util.Set;

import oracle.AccessedDOMNode;
import oracle.Attribute;
import oracle.FunctionPoint;
import oracle.Variable;



public class QunitAssertion {
	
	public static enum AssertionType {ok, equal, deepEqual};
	String assertionCodeForVariable="";
	String assertioncodeForDom="";

	public QunitAssertion(){
		
	}
	
	public void makeQunitAssertionForVariable(String actual, String expected, AssertionType assertionType){

		String assertionCode="";
		if(assertionType.name().equals(AssertionType.ok)){
			
			assertionCode=assertionType.toString() + "(" + actual +" == " + expected + ", " + "" +")" + ";";
		}
		else{
			
			assertionCode=assertionType.toString() + "(" + actual +", " + expected + ", " + "" +")" + ";";
		}
		assertionCodeForVariable=assertionCode;
	
		
		
	}
	
	public void makeQunitAssertionForDomNode(AccessedDOMNode expectedAccessedDomNode){

			
			String xpath=expectedAccessedDomNode.xpath;
			Set<Attribute> attrs=expectedAccessedDomNode.getAllAttibutes();
			String code=getSelectElementByXpathCode(xpath);
		
				
			String assertionCode=code+"\n";
			assertionCode+=AssertionType.ok.toString() + "(" +"node.length>0" + ", " + "" +")" + ";" + "\n";
			Iterator<Attribute> iter=attrs.iterator();
			while(iter.hasNext()){
				Attribute attr=iter.next();
				String attrName=attr.getAttrName();
				String attrValue=attr.getAttrValue();
				if(attrName.equals("tagName")){
					
					String actual="node.prop" + "(" +"'" + attrName + "'" + ")";
					String expected=attrValue;
					assertionCode+=AssertionType.equal.toString() + "(" + actual +", " + expected + ", " + "" +")" + ";";
					assertionCode+="\n";
				}
				
				else{
					String actual="node.attr" + "(" +"'" + attrName + "'" + ")";
					String expected=attrValue;
					assertionCode=AssertionType.equal.toString() + "(" + actual +", " + expected + ", " + "" +")" + ";";
					assertionCode+="\n";
				}
			}
				
			assertioncodeForDom=assertionCode;
		
			
			
		}


	

	
	private String getSelectElementByXpathCode(String xpath){
		String code="";
		code= "evaluated=document.evaluate" + "(" + xpath + ", " + 
		"document" + ", " + "null" +", " +"XPathResult.ANY_TYPE" + ", " + "null" + ")" + ";" + "\n";
		code+= "node = $(evaluated.iterateNext());";
		return code;
	}
	
	public String getAssertionCodeForVariable(){
		return assertionCodeForVariable;
	}
	
	public String getAssertionCodeForDom(){
		return assertioncodeForDom;
	}
}

