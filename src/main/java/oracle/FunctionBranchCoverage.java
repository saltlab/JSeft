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
}
