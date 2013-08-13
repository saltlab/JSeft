package oracle;

import java.util.ArrayList;
import java.util.List;

public class FunctionBranchCoverage {
	
	public String functionName;
	public List<BranchCoverage> coveredBranches;
	
	public FunctionBranchCoverage(String funcName){
		
		functionName=funcName;
		coveredBranches=new ArrayList<BranchCoverage>();
	}

	public void addCoveredBranch(BranchCoverage brCovg){
		coveredBranches.add(brCovg);
	}
	
	public boolean isSameFunctionBrCovg(FunctionBranchCoverage funcBrCov){
		
		boolean same=false;
		List<BranchCoverage> funcBranches=funcBrCov.coveredBranches;
		for(BranchCoverage br:funcBranches){
			same=false;
			for(BranchCoverage thisBr:coveredBranches){
				if(thisBr.isSameCoverage(br)){
					same=true;
					break;
				}
			}
			if(!same){
				return false;
			}
		}
		
		return true;
		
	}
}
