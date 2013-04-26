package qunitGenerator;

import java.util.ArrayList;
import java.util.List;

public class CombinedAssertions {

	private List<IndividualAssertions> individualAssertions=new ArrayList<IndividualAssertions>();
	
	public void addIndividualAssertions(IndividualAssertions individualAssertion){
		this.individualAssertions.add(individualAssertion);
	}
	public List<IndividualAssertions> getIndividualAssertions(){
		return individualAssertions;
	}
	
	
}
