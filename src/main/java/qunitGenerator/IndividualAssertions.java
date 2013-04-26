package qunitGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.AccessedDOMNode;

public class IndividualAssertions {

	private List<ArrayList<String>> actualExpectedList=new ArrayList<ArrayList<String>>();
	private Set<AccessedDOMNode> accessedDomNodes=new HashSet<AccessedDOMNode>(); 
	public void addIndividualAssertions(String actual, String expected, Set<AccessedDOMNode> accessedDomNodes){
		ArrayList<String> actualExpected=new ArrayList<String>();
		actualExpected.add(actual);
		actualExpected.add(expected);
		actualExpectedList.add(actualExpected);
		this.accessedDomNodes=accessedDomNodes;
	}
	
	public List<ArrayList<String>> getActualExpectedList(){
		return actualExpectedList;
	}
	
	public Set<AccessedDOMNode> getAccessedDomNodes(){
		return accessedDomNodes;
	}

}
