package oracle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class FunctionStateComparator {
	/* (funcName->(entrypoint->oracle)) */
	private ArrayListMultimap<String,ArrayListMultimap<FunctionPoint,Oracle>> oracleMultimap=ArrayListMultimap.create();	
//	private Multimap<String, FunctionState> funcNameToFuncStateMap_modifiedVer;
	/* (funcName->(entrypoint->exitpoint)) */
/*	private ArrayListMultimap<String,ArrayListMultimap<FunctionPoint,FunctionPoint>> oracleMultimap=ArrayListMultimap.create();	
	public FunctionStateComparator(Multimap<String, FunctionState> funcNameToFuncStateMap_modifiedVer){
		this.funcNameToFuncStateMap_modifiedVer=funcNameToFuncStateMap_modifiedVer;
		
	}
	
	public Multimap<String, FunctionState> getFuncNameToFuncStateMap_modifiedVer(){
		return funcNameToFuncStateMap_modifiedVer;
	}
*/	
	public void analysingOutputDiffs(){
		
		Set<String> keys=MutatedJsExecTraceAnalyser.funcNameToFuncStateMap_modifiedVer.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
		
			String funcName=iter.next();
			List<FunctionState> funcStates=(List<FunctionState>) MutatedJsExecTraceAnalyser.funcNameToFuncStateMap_modifiedVer.get(funcName);
			Multimap<FunctionPoint, FunctionPoint> funcEntryToMultiExit=
					OriginalJsExecTraceAnalyser.funcEntryPointToExitPointMap.get(funcName);
			
		
			for(int i=0;i<funcStates.size();i++){
				FunctionState modifiedFuncState=funcStates.get(i);
				FunctionPoint modifiedFuncEntry=modifiedFuncState.getFunctionEntry();
				FunctionPoint modifiedFuncExit=modifiedFuncState.getFunctionExit();
				FunctionPoint origFuncEntry = null;
				ArrayList<FunctionPoint> origFuncExits=getFunctinExitsMatchedWithEntryPoint(funcEntryToMultiExit, modifiedFuncEntry, origFuncEntry);
				boolean same=false;
				for(int j=0;j<origFuncExits.size();j++){
					FunctionPoint origFuncExit=origFuncExits.get(j);
					Oracle oracle=addDiffPartsToTheOracleFuncExits(modifiedFuncExit, origFuncExit);
					if(oracle.getVariables().size()>0 || oracle.getAccessedDomNodes().size()>0){
						
					}
					
					
					
		/*			same=functionPointsSimilar(modifiedFuncExit, origFuncExit);
					if (same)
						break;
		*/			
		/*			if(!same){
						
						ArrayListMultimap<FunctionPoint,FunctionPoint> funcPointMltimap=ArrayListMultimap.create();
						funcPointMltimap.put(origFuncEntry, origFuncExit);
						oracleMultimap.put(funcName, funcPointMltimap);
						break;
					}
		*/			
				}

				boolean isFuncEntryRepeatedInOracle=isEntryPointRepeatedInOracleSet(funcName,origFuncEntry);
				if(!same && !isFuncEntryRepeatedInOracle){

					ArrayListMultimap<FunctionPoint,FunctionPoint> funcPointMltimap=ArrayListMultimap.create();
					for(int j=0;j<origFuncExits.size();j++){
						
						FunctionPoint origFuncExit=origFuncExits.get(j);
						funcPointMltimap.put(origFuncEntry, origFuncExit);
						
					}
					oracleMultimap.put(funcName, funcPointMltimap);
				}
				
				
			}
			
		}
		
	}
	
	private Oracle addDiffPartsToTheOracleFuncExits(FunctionPoint modifiedFuncExit, FunctionPoint origFuncExit) {
		
		Oracle oracle=new Oracle();
		if(modifiedFuncExit.getPointName().equals(origFuncExit.getPointName())){
			ArrayList<Variable> origvarList=origFuncExit.getVariables();
			ArrayList<Variable> modifvarList=modifiedFuncExit.getVariables();
			for(int i=0;i<origvarList.size();i++){
				boolean sameVariable=false;;
				for(int j=0;j<modifvarList.size();j++){
					if(origvarList.get(i).getValue().equals(modifvarList.get(j).getValue()) 
							&& origvarList.get(i).equals(modifvarList.get(j).getType())){
						sameVariable=true;
						break;
					}
				}
				if(!sameVariable){
					oracle.addVariable(origvarList.get(i));
				}
				
			}
			
			ArrayList<AccessedDOMNode> origAccessedDomNodeList=origFuncExit.getAccessedDomNodes();
			ArrayList<AccessedDOMNode> modifAccessedDomNodeList=modifiedFuncExit.getAccessedDomNodes();
			for(AccessedDOMNode origAccessedDomNode:origAccessedDomNodeList){
				boolean sameXpath=false;
				AccessedDOMNode sameModifAccessedDomNode=null;
				for(AccessedDOMNode modifAccessedDomNode:modifAccessedDomNodeList){
					if(origAccessedDomNode.xpath.equals(modifAccessedDomNode.xpath)){
						sameXpath=true;
						sameModifAccessedDomNode=modifAccessedDomNode;
						break;
					}
					
				}
				AccessedDOMNode oracleAccessedDomNode=new AccessedDOMNode();
				if(sameXpath){
					Set<Attribute> modifAttrs=sameModifAccessedDomNode.getAllAttibutes();
					Set<Attribute> origAttrs=origAccessedDomNode.getAllAttibutes();
					Iterator<Attribute> iter=origAttrs.iterator();
					
					while(iter.hasNext()){
						Attribute origAttr=iter.next();
						Iterator<Attribute> modifIter=modifAttrs.iterator();
						boolean sameAttr=false;
					
						while(modifIter.hasNext()){
							Attribute modifAttr=modifIter.next();
							if(origAttr.getAttrName().equals(modifAttr.getAttrName()) &&
									origAttr.getAttrValue().equals(modifAttr.getAttrValue())){
								sameAttr=true;
								break;
							}
							
						}
						
						if(!sameAttr){
							oracleAccessedDomNode.xpath=origAccessedDomNode.xpath;
							oracleAccessedDomNode.addAttribute(origAttr);
						}
					}
				}
				
				else{
					
					oracleAccessedDomNode.xpath=origAccessedDomNode.xpath;
				}
				
				//meaning that this accessedDomNode has something to be included in our assertions
				if(!oracleAccessedDomNode.xpath.equals("")){
					oracle.addAccessedDomNode(oracleAccessedDomNode);
				}
			}
		
		
		
		
		}
		
		return oracle;
		
	}

	private boolean functionPointsSimilar(FunctionPoint funcPoint1, FunctionPoint funcPoint2){
		if(funcPoint1.getPointName().equals(funcPoint2.getPointName())){
			ArrayList<Variable> varList1=funcPoint1.getVariables();
			ArrayList<Variable> varList2=funcPoint2.getVariables();
			
			if(funcPoint1.getPointName().toLowerCase().equals("entry")){
				String domHtml1=funcPoint1.getDomHtml();
				String domHtml2=funcPoint2.getDomHtml();
				if(varList1.equals(varList2) && domHtml1.equals(domHtml2))
					return true;
			}
			
			else if(funcPoint1.getPointName().toLowerCase().equals("exit")){
				ArrayList<AccessedDOMNode> accessedNodes1=funcPoint1.getAccessedDomNodes();
				ArrayList<AccessedDOMNode> accessedNodes2=funcPoint2.getAccessedDomNodes();
				if(varList1.equals(varList2) && accessedNodes1.equals(accessedNodes2)){
					return true;
				}
			}
		}
		return false;
	}
	
	private ArrayList<FunctionPoint> getFunctinExitsMatchedWithEntryPoint(Multimap<FunctionPoint, FunctionPoint> funcEntryToMultiExit, FunctionPoint funcPoint, FunctionPoint origFuncEntry){
		
		ArrayList<FunctionPoint> exitFuncPoints=new ArrayList<FunctionPoint>();
		Set<FunctionPoint> keys=funcEntryToMultiExit.keySet();
		Iterator<FunctionPoint> iter=keys.iterator();
		
		while(iter.hasNext()){
			
			FunctionPoint entryPoint=iter.next();
			if(entryPoint.getPointName().equals(funcPoint.getPointName())){
				ArrayList<Variable> origVars=entryPoint.getVariables();
				ArrayList<Variable> modifiedVars=funcPoint.getVariables();
/*				ArrayList<AccessedDOMNode> origNodes=entryPoint.getAccessedDomNodes();
				ArrayList<AccessedDOMNode> modifiedNodes=funcPoint.getAccessedDomNodes();
*/				
				String origDomHtml=entryPoint.getDomHtml();
				String modifiedDomHtml=funcPoint.getDomHtml();
				if(origVars.equals(modifiedVars) && origDomHtml.equals(modifiedDomHtml)){
					origFuncEntry=entryPoint;
					exitFuncPoints.addAll(funcEntryToMultiExit.get(entryPoint));
					break;
				}
			}
		}
		
		return exitFuncPoints;
		
	}
	
	@Deprecated
	private FunctionPoint getFunctionMatchedEntryPoint(Multimap<FunctionPoint, FunctionPoint> funcEntryToMultiExit, FunctionPoint modifiedFuncPoint){
		
		
		Set<FunctionPoint> keys=funcEntryToMultiExit.keySet();
		Iterator<FunctionPoint> iter=keys.iterator();
		
		while(iter.hasNext()){
			
			FunctionPoint entryPoint=iter.next();
			if(entryPoint.getPointName().equals(modifiedFuncPoint.getPointName())){
				ArrayList<Variable> origVars=entryPoint.getVariables();
				ArrayList<Variable> modifiedVars=modifiedFuncPoint.getVariables();
/*				ArrayList<AccessedDOMNode> origNodes=entryPoint.getAccessedDomNodes();
				ArrayList<AccessedDOMNode> modifiedNodes=modifiedFuncPoint.getAccessedDomNodes();
*/				
				
				String origDomHtml=entryPoint.getDomHtml();
				String modifiedDomHtml=modifiedFuncPoint.getDomHtml();
				if(origVars.equals(modifiedVars) && origDomHtml.equals(modifiedDomHtml)){
					
					return entryPoint;
					
				}
			}
		}
		
		return null;
		
	}
	
	private boolean isEntryPointRepeatedInOracleSet(String funcName, FunctionPoint origFuncEntry){
		
		List<ArrayListMultimap<FunctionPoint, Oracle>> entryOracleList=oracleMultimap.get(funcName);
		for(ArrayListMultimap<FunctionPoint, Oracle> entryOracle:entryOracleList){
			
			Set<FunctionPoint> key=entryOracle.keySet();
			Iterator<FunctionPoint> iterator=key.iterator();
			while(iterator.hasNext()){
				FunctionPoint entryPoint=iterator.next();
				if(entryPoint.getPointName().equals(origFuncEntry.getPointName())){
					if(entryPoint.getVariables().equals(origFuncEntry.getVariables())
							&& entryPoint.getDomHtml().equals(origFuncEntry.getDomHtml())){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
}
