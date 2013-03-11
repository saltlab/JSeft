package oracle;

import com.google.common.collect.Multimap;

public class FunctionStateComparator {

	
	private Multimap<String, FunctionState> funcNameToFuncStateMap_modifiedVer;
	
	public FunctionStateComparator(Multimap<String, FunctionState> funcNameToFuncStateMap_modifiedVer){
		this.funcNameToFuncStateMap_modifiedVer=funcNameToFuncStateMap_modifiedVer;
		
	}
	
	public Multimap<String, FunctionState> getFuncNameToFuncStateMap_modifiedVer(){
		return funcNameToFuncStateMap_modifiedVer;
	}
	
	public void analysingOutputDiffs(){
		
	}
	
}
