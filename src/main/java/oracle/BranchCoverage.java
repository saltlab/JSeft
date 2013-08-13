package oracle;

public class BranchCoverage {


	public String lineNo;
	public String covered;
	
	public BranchCoverage(String lineNo, String covered){
		this.lineNo=lineNo;
		this.covered=covered;
	}
	
	public boolean isSameCoverage(BranchCoverage brCov){
		if(brCov.lineNo.equals(this.lineNo)){
			if(!brCov.covered.equals(this.covered))
				return false;
			else
				return true;
		}
		return false;
	}
	
}
