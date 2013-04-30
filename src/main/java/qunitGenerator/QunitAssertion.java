package qunitGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oracle.AccessedDOMNode;
import oracle.Attribute;
import oracle.FunctionPoint;
import oracle.Variable;



public class QunitAssertion {
	
	public static enum AssertionType {ok, equal, deepEqual};
	private String assertionCodeForVariable="";
	private String assertioncodeForDom="";
	private ArrayList<String> assertionCodes=new ArrayList<String>();

	public QunitAssertion(){
		
	}
	
	public ArrayList<String> getAssertionCodes(){
		return assertionCodes;
	}
	public int getTotalNumberOfAssertions(){
		return assertionCodes.size();
	}
	
	public void makeQunitAssertionForVariable(String actual, String expected, AssertionType assertionType){

		String assertionCode="";
		
		String msg="\"" + expected.replaceAll("\"", "\\\\\"") + " is expected but " + "\"" + " + " + actual.replaceAll("==.*", "").replaceAll("\"", "\\\"") + " + " + "\""+ " is returned" + "\"";
		if(assertionType.name().equals(AssertionType.ok.toString())){
			
			assertionCode=assertionType.toString() + "(" + actual + ", " + msg +")" + ";";
			
		}
		else{
			
			assertionCode=assertionType.toString() + "(" + actual +", " + expected + ", " + msg +")" + ";";
			
		}
		assertionCodeForVariable=assertionCode;
		assertionCodes.add(assertionCode);
	
		
		
	}
	
	public void makeQunitAssertionForDomNode(AccessedDOMNode expectedAccessedDomNode, int counter){

			
			String xpath=expectedAccessedDomNode.xpath;
			Set<Attribute> attrs=expectedAccessedDomNode.getAllAttibutes();
			String code=getSelectElementByXpathCode(xpath, counter);
		
				
			String assertionCode=code+"\n" + "\t";

			String msg="\"" + "node" + counter +".length is " + "\"" + "+" + "node"+ counter + ".length";
			assertionCode+=AssertionType.ok.toString() + "(" +"node" + counter + ".length>0" + ", " + msg +")" + ";" + "\n" + "\t";
			Iterator<Attribute> iter=attrs.iterator();
			while(iter.hasNext()){
				Attribute attr=iter.next();
				String attrName=attr.getAttrName();
				String attrValue=attr.getAttrValue();
				
					
				if(attrName.equals("tagName")){
					
					String actual="node" + counter +".prop" + "(" +"'" + attrName + "'" + ")";
					String expected="'"+ attrValue + "'";
					msg="\"" + expected.replaceAll("\"", "\\\\\"") + " is expected but " + "\"" + " + " + actual.replaceAll("\"", "\\\"") + " + " + "\""+ " is returned" + "\"";
					assertionCode+=AssertionType.deepEqual.toString() + "(" + actual +", " + expected + ", " + msg +")" + ";";
					assertionCode+="\n\n" + "\t";
					assertionCodes.add(assertionCode);
				}
				
				else{
					String actual="node" + counter + ".attr" + "(" +"'" + attrName + "'" + ")";
					String expected;
	/*				if(attrValue.equals("EMPTY")){
						attrValue="\"\"";
						 expected=attrValue;
					}
	*/	//			else{
						expected="'" + attrValue + "'";
		//			}
					msg="\"" + expected.replaceAll("\"", "\\\\\"") + " is expected but " + "\"" + " + " + actual.replaceAll("\"", "\\\"") + " + " + "\""+ " is returned" + "\"";
				
					assertionCode+=AssertionType.deepEqual.toString() + "(" + actual +", " + expected + ", " + msg +")" + ";";
					assertionCode+="\n\n" + "\t";
					assertionCodes.add(assertionCode);
				}
			}
				
			assertioncodeForDom=assertionCode;
			assertionCodes.add(assertionCode);
		
			
			
		}


	

	
	private String getSelectElementByXpathCode(String xpath, int counter){
	
		String xpathToEvaluate="";
		if(!xpath.startsWith("//")){
			if(xpath.startsWith("/")){
				xpathToEvaluate=xpath.replace("/html/body/", "/html/body/div/");
			}
		}
		else
			xpathToEvaluate=xpath;
		String code="";
		/* div added because of <div id="qunit-fixture"></div> */
//		xpathToEvaluate="//div[@id='qunit-fixture']" + xpath;
		code= "var evaluated" + counter +"=document.evaluate" + "(" + "\"" + xpathToEvaluate + "\"" + ", " + 
		"document" + ", " + "null" +", " +"XPathResult.ANY_TYPE" + ", " + "null" + ")" + ";" + "\n" +"\t";
		code+= "var node" + counter + "= $(evaluated" + counter + ".iterateNext());";
		return code;
	}
	
	public String getAssertionCodeForVariable(){
		return assertionCodeForVariable;
	}
	
	public String getAssertionCodeForDom(){
		return assertioncodeForDom;
	}
	
	
	
	
	public void makeCombinedQunitAssertion(CombinedAssertions combinedAssertion){

		String assertionCode="";
		String msg="\"\"";
	
		assertionCode=AssertionType.ok.toString() + "(";
		
	
		List<IndividualAssertions> individualAssertions=combinedAssertion.getIndividualAssertions();
		for(IndividualAssertions individualAssertion:individualAssertions){
			List<ArrayList<String>> listOfActualExpected=individualAssertion.getActualExpectedList();
			Set<AccessedDOMNode> listOfNodes=individualAssertion.getAccessedDomNodes();
			assertionCode+="(";
			for(ArrayList<String> actualExpected:listOfActualExpected){
				String actual=actualExpected.get(0);
				String expected=actualExpected.get(1);	
				assertionCode+= "("+ actual + "==" + expected  +")" + " && ";
					
			}
			
			assertionCode=assertionCode.substring(0, assertionCode.length()-3);
			
			/////
			for(AccessedDOMNode expectedAccessedDomNode:listOfNodes){
	//			String xpath=expectedAccessedDomNode.xpath;
				Set<Attribute> attrs=expectedAccessedDomNode.getAllAttibutes();
	/*			String code=getSelectElementByXpathCode(xpath);
				assertionCode+="(" +"node.length>0" +")" + " && ";
	*/			Iterator<Attribute> iter=attrs.iterator();
				while(iter.hasNext()){
					Attribute attr=iter.next();
					String attrName=attr.getAttrName();
					String attrValue=attr.getAttrValue();
					if(attrName.equals("tagName")){
						
						String actual="node.prop" + "(" +"'" + attrName + "'" + ")";
						String expected=attrValue;
						assertionCode+="(" + actual + "==" + expected + ")" + " && ";
						
					}
					
					else{
						String actual="node.attr" + "(" +"'" + attrName + "'" + ")";
	/*					if(attrValue.equals("EMPTY"))
						attrValue="\"\"";
	*/					String expected=attrValue;

					
						assertionCode+="(" + actual +"==" + expected + ")" + " && ";
						
					}
				}
				
				assertionCode=assertionCode.substring(0, assertionCode.length()-3);
			}
			
			/////
						
			assertionCode=assertionCode.substring(0, assertionCode.length()-1);
			assertionCode+=")";
			assertionCode+=" || ";
		}
		assertionCode=assertionCode.substring(0, assertionCode.length()-3);
		assertionCode+=", "+ msg + ")" + ";";

		assertionCodeForVariable=assertionCode;
		assertionCodes.add(assertionCode);
	
		
		
	}
}

