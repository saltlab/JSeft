package oracle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sun.invoke.util.VerifyAccess;

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
	
	public ArrayListMultimap<String,ArrayListMultimap<FunctionPoint,Oracle>> getOracleMultimap(){
		return oracleMultimap;
	}
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
				FunctionPoint origFuncEntry=null;
				ArrayList<FunctionPoint> origFuncExits=getFunctinExitsMatchedWithEntryPoint(funcEntryToMultiExit, modifiedFuncEntry);
				// the last element in origFuncExists is the origFuncEntry
				int origFuncExitsSize=origFuncExits.size()-1;
				if(origFuncExits.size()>0)
					origFuncEntry=origFuncExits.get(origFuncExitsSize);
				boolean same=true;
				// Note: the last element in origFuncExists is the origFuncEntry
				for(int j=0;j<origFuncExitsSize;j++){
					
					FunctionPoint origFuncExit=origFuncExits.get(j);	
					same=functionPointsSimilar(modifiedFuncExit, origFuncExit);
					if (!same)
						break;					
				}

				if(!same){
					
					for(int j=0;j<origFuncExitsSize;j++){
						FunctionPoint origFuncExit=origFuncExits.get(j);
						Oracle newOracle=addDiffPartsToTheOracleFuncExits(modifiedFuncExit, origFuncExit);
						
						updateExistingnOracleSet(funcName, origFuncEntry, newOracle);
					}
					
				}
				
				
/*				if(!same && !isFuncEntryRepeatedInOracle){

					ArrayListMultimap<FunctionPoint,FunctionPoint> funcPointMltimap=ArrayListMultimap.create();
					for(int j=0;j<origFuncExits.size();j++){
						
						FunctionPoint origFuncExit=origFuncExits.get(j);
						funcPointMltimap.put(origFuncEntry, origFuncExit);
						
					}
					oracleMultimap.put(funcName, funcPointMltimap);
				}
*/								
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
					if(origvarList.get(i).getVariableName().equals(modifvarList.get(j).getVariableName())
							&& origvarList.get(i).getValue().equals(modifvarList.get(j).getValue()) 
							&& origvarList.get(i).equals(modifvarList.get(j).getType())){
						sameVariable=true;
						break;
					}
				}
				if(!sameVariable){
					oracle.addVariable(origvarList.get(i));
					oracle.setOrigVersionExitFuncPoint(origFuncExit);
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
					oracle.setOrigVersionExitFuncPoint(origFuncExit);	
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
				
				
				if(variableListsSimilar(varList1, varList2)) //&& domHtml1.equals(domHtml2))
					return true;
			}
			
			else if(funcPoint1.getPointName().toLowerCase().equals("exit")){
				ArrayList<AccessedDOMNode> accessedNodes1=funcPoint1.getAccessedDomNodes();
				ArrayList<AccessedDOMNode> accessedNodes2=funcPoint2.getAccessedDomNodes();
			
				if(variableListsSimilar(varList1, varList2) && accessedDomNodeListsSimilar(accessedNodes1, accessedNodes2)){
					return true;
				}
			}
		}
		return false;
	}
	
	private ArrayList<FunctionPoint> getFunctinExitsMatchedWithEntryPoint(Multimap<FunctionPoint, FunctionPoint> funcEntryToMultiExit, FunctionPoint funcPoint){
		
		FunctionPoint origFuncEntry = null;
		ArrayList<FunctionPoint> exitFuncPoints=new ArrayList<FunctionPoint>();
		Set<FunctionPoint> keys=funcEntryToMultiExit.keySet();
		Iterator<FunctionPoint> iter=keys.iterator();
		
		while(iter.hasNext()){
			
			FunctionPoint entryPoint=iter.next();
			ArrayList<Variable> origVars=entryPoint.getVariables();
			if(entryPoint.getPointName().equals(funcPoint.getPointName())){
				
				ArrayList<Variable> modifiedVars=funcPoint.getVariables();
/*				ArrayList<AccessedDOMNode> origNodes=entryPoint.getAccessedDomNodes();
				ArrayList<AccessedDOMNode> modifiedNodes=funcPoint.getAccessedDomNodes();
*/				
				
				String origDomHtml=entryPoint.getDomHtml();
				String modifiedDomHtml=funcPoint.getDomHtml();
				if(variableListsSimilar(origVars,modifiedVars) ){//&& origDomHtml.equals(modifiedDomHtml)){
					origFuncEntry=entryPoint;
					exitFuncPoints.addAll(funcEntryToMultiExit.get(entryPoint));
					break;
				}
			}
		}
		
		if(origFuncEntry!=null)
			exitFuncPoints.add(origFuncEntry);
		// the last element in exitFuncPoints is the origFuncEntry
		return (exitFuncPoints);
		
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
				if(variableListsSimilar(origVars, modifiedVars) && origDomHtml.equals(modifiedDomHtml)){
					
					return entryPoint;
					
				}
			}
		}
		
		return null;
		
	}
	
	private void updateExistingnOracleSet(String funcName, FunctionPoint origFuncEntry, Oracle newOracle){
		
		if(newOracle.getAccessedDomNodes().size()==0 && newOracle.getVariables().size()==0)
			return;
		List<ArrayListMultimap<FunctionPoint, Oracle>> entryOracleList=oracleMultimap.get(funcName);
		for(ArrayListMultimap<FunctionPoint, Oracle> entryOracle:entryOracleList){
			
			Set<FunctionPoint> key=entryOracle.keySet();
			Iterator<FunctionPoint> iterator=key.iterator();
			while(iterator.hasNext()){
				FunctionPoint entryPoint=iterator.next();
				if(entryPoint.getPointName().equals(origFuncEntry.getPointName())){
					if(variableListsSimilar(entryPoint.getVariables(), origFuncEntry.getVariables())
							&& entryPoint.getDomHtml().equals(origFuncEntry.getDomHtml())){
						List<Oracle> oracleList=entryOracle.get(entryPoint);
						for(Oracle oracle:oracleList){
							FunctionPoint origFuncExitPoint=oracle.getOrigVersionExitFuncPoint();
							FunctionPoint newOracleFuncExitPoint=newOracle.getOrigVersionExitFuncPoint();
							
							if(variableListsSimilar(origFuncExitPoint.getVariables(), newOracleFuncExitPoint.getVariables())
									&& origFuncExitPoint.getDomHtml().equals(newOracleFuncExitPoint.getDomHtml())){
								
								oracle.addVariableSet(newOracle.getVariables());
								oracle.addAccessedDomNodeSet(newOracle.getAccessedDomNodes());
								return;
							}
						}
					}
				}
			}
			

			
			
		}
		
		/* at this point we know that this function entry has not been existed in the current oracle map,
		 * otherwise it should have been returned before */
		ArrayListMultimap<FunctionPoint,Oracle> funcPointMltimap=ArrayListMultimap.create();
		funcPointMltimap.put(origFuncEntry, newOracle);
		oracleMultimap.put(funcName, funcPointMltimap);
		

		
	}
	
	private boolean variableListsSimilar(ArrayList<Variable> varList1, ArrayList<Variable> varList2){
		
		Set<Variable> varSet1 = new HashSet<Variable>(varList1);
		Set<Variable> varSet2 = new HashSet<Variable>(varList2);
		if(varSet1.equals(varSet2))
			return true;
		return false;
		
	}
	
	private boolean accessedDomNodeListsSimilar(ArrayList<AccessedDOMNode> accessedDomList1, ArrayList<AccessedDOMNode> accessedDomList2){
		
		Set<AccessedDOMNode> accessedDomSet1 = new HashSet<AccessedDOMNode>(accessedDomList1);
		Set<AccessedDOMNode> accessedDomSet2 = new HashSet<AccessedDOMNode>(accessedDomList2);
		if(accessedDomSet1.equals(accessedDomSet2))
			return true;
		return false;
		
	}
	
	
}
