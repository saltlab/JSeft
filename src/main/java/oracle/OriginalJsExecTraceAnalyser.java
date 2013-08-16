package oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import domMutation.Node;

import executionTracer.DOM_JS_ExecutionTracer;
import executionTracer.JSExecutionTracer;

public class OriginalJsExecTraceAnalyser extends JsExecTraceAnalyser{
	
	public static Multimap<String, FunctionState> funcNameToFuncStateMap;

	private Multimap<String, FunctionPoint> funcNameToFuncPointMap;
	public static ArrayList<String> functionListOfOriginalVersion=new ArrayList<String>();
	public static HashMap<String, ArrayListMultimap<FunctionPoint,FunctionPoint>> funcEntryPointToExitPointMap=new HashMap<String, ArrayListMultimap<FunctionPoint,FunctionPoint>>();
	
	public OriginalJsExecTraceAnalyser(String outputFolder){
		
		
		super(outputFolder);
		createFuncEntryToFuncExitMap();
	}
	

	public Multimap<String, FunctionState> getFuncNameToFuncStateMap(){
		return funcNameToFuncStateMap;
	}
	
	@Override
	protected List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(outputFolder +  DOM_JS_ExecutionTracer.EXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".txt")) {
				result.add(outputFolder + DOM_JS_ExecutionTracer.EXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}
	
	@Override
	protected void startAnalysingJsExecTraceFiles(){
		try{
			funcNameToFuncStateMap=ArrayListMultimap.create();
			funcNameToFuncPointMap=ArrayListMultimap.create();
			List<String>filenameAndPathList=getTraceFilenameAndPath();
			List<Object[]> funcListToSort=	new ArrayList<Object[]>();
			for (String filenameAndPath:filenameAndPathList){
				BufferedReader input =
					new BufferedReader(new FileReader(filenameAndPath));
				
				String line="";
				while ((line = input.readLine()) != null){
					
					String[] funcNameLine=line.split(":::");
					String funcName=funcNameLine[0];
					String pointName=funcNameLine[1];
					long time=0;
					String variableName="";
					String type="";
					String value="";
					String variableUsage="";
					String domHtml="";
/*					String nodeLine="";
					String nodeValue="";
*/					
					Variable varibale;
//					AccessedDOMNode accessedDomNode=null;

					ArrayList<Variable> variables=new ArrayList<Variable>();
					ArrayList<AccessedDOMNode> domNodes=new ArrayList<AccessedDOMNode>();
					
					FunctionPoint functionPoint;
//					FunctionPoint domRelatedFunctionPoint;
					FunctionBranchCoverage funcBrnCovg = new FunctionBranchCoverage(funcName);
					while (!(line = input.readLine()).equals
							("===========================================================================")){

						if(line.contains("time::")){
						
							time=Long.valueOf(line.split("::")[1]);
						}
						else if(line.contains("branchCoverage::")){
							funcBrnCovg=new FunctionBranchCoverage(funcName);
							String brCovgline=line.split("::")[1].replace("{", "").replace("}", "").replace("\"", "");
							String[] funcName_lineNo_covered=brCovgline.split(",");
							for(String str:funcName_lineNo_covered){
								
								String[] strArray=str.split(":");
								String covered=strArray[strArray.length-1];
								String lineNo;
								if(covered.equals("")){
									lineNo="-1";
									covered="-1";
								}
								else{
									String funcName_lineNo=str.replace(":"+covered, "");
									String[] funcName_lineNoArr=funcName_lineNo.split("_");
									lineNo=funcName_lineNoArr[funcName_lineNoArr.length-1];
								}
								BranchCoverage brCov=new BranchCoverage(lineNo, covered);
								funcBrnCovg.addCoveredBranch(brCov);
							}
							
							
							
						}
						else if(line.contains("variable::")){
							variableName=line.split("::")[1];
						}
						else if(line.contains("type::")){
							type=line.split("::")[1];
						}
						else if(line.contains("value::")){
							value=line.split("::")[1];
						}
						
						else if(line.contains("variableUsage::")){
							variableUsage=line.split("::")[1];
						}
						else if(line.contains("dom::")){
							domHtml=line.split("dom::")[1];
						}
						else if(line.contains("node::")){
							String node=line.split("node::")[1];
							ObjectMapper mapper = new ObjectMapper();
						    AccessedDOMNode domNode = mapper.readValue(node, AccessedDOMNode.class);  
						    mapper.writeValueAsString(domNode);
						    domNode.makeAllAttributes();
						    domNodes.add(domNode);
						
						}
/*						else if(line.contains("line::")){
							nodeLine=line.replace("line::", "");
						}
						else if(line.contains("value::")){
							nodeValue=line.replace("value::", "");
						}
						
*/						
						
						
						if(variableName!="" && value!="" && type!="" && variableUsage!=""){
							varibale=new Variable(variableName, value, type, variableUsage);
							variables.add(varibale);
							variableName="";
							value="";
							type="";
							variableUsage="";
							
							
						}
/*						if(domNode!=null){
							accessedDomNode=new AccessedDOMNode(domNode);
							
						}
*/						
					
					}
					
/*					if(accessedDomNode!=null){
						addingDomRelatedFunctionPoint(accessedDomNode, funcName, pointName, time);
					}
					else{
						addingNonDomRelatedFunctionPoint(variables, funcName, pointName, time);
					}
*/					
					
					
//					List<FunctionPoint> functionPoints=(List<FunctionPoint>) funcNameToFuncPointMap.get(funcName);
//					java.util.Collections.sort(functionPoints, bvc);

					
				functionPoint=new FunctionPoint(pointName, variables, domHtml, time, funcBrnCovg);

				Object[] funcPointFuncName=new Object[2];
				funcPointFuncName[0]=functionPoint;
				funcPointFuncName[1]=funcName;
								
				funcListToSort.add(funcPointFuncName);
				java.util.Collections.sort(funcListToSort, funcNameFuncPointComp);
				
				if(domNodes.size()>0){
					functionPoint.addAccessedDomNodes(domNodes);
				}
		//		funcNameToFuncPointMap.put(funcName, functionPoint);
				}
				input.close();
			  }
			
			for(int i=0; i<funcListToSort.size();i++){
				FunctionPoint fPoint=(FunctionPoint) funcListToSort.get(i)[0];
				String fName=(String) funcListToSort.get(i)[1];
				if(fPoint.getPointName().toLowerCase().equals("enter")){
					for(int j=i+1; j<funcListToSort.size(); j++){
						FunctionPoint fNextPoint=(FunctionPoint) funcListToSort.get(j)[0];
						String fNextName=(String) funcListToSort.get(j)[1];
						if(fNextName.equals(fName) && fNextPoint.getPointName().toLowerCase().equals("exit")){
							break;
						}
						else 
							if(!fNextName.equals(fName) && fNextPoint.getPointName().toLowerCase().equals("enter")){
								fPoint.addGlobVariableIfNotExist(fNextPoint.getVariables());
						//		fPoint.addAccessedDomNodes(fNextPoint.getAccessedDomNodes());
								
							}
					}
				}
			}
			
			for(int i=0;i<funcListToSort.size();i++){
				FunctionPoint fPoint=(FunctionPoint) funcListToSort.get(i)[0];
				String fName=(String) funcListToSort.get(i)[1];
				List<FunctionPoint> funcList=(List<FunctionPoint>) funcNameToFuncPointMap.get(fName);
		
				funcNameToFuncPointMap.put(fName, fPoint);
		
			}
			
			Set<String> keys=funcNameToFuncPointMap.keySet();
			Iterator<String> it=keys.iterator();
			while(it.hasNext()){
				String funcName=it.next();
				List<FunctionPoint> points=(List<FunctionPoint>) funcNameToFuncPointMap.get(funcName);
				java.util.Collections.sort(points, vc);
			}
			
	/*		Set<String> keysets=funcNameToFuncPointMap.keySet();
			it=keysets.iterator();
			while(it.hasNext()){
				String funcName=it.next();
				Collection<FunctionPoint> points=funcNameToFuncPointMap.get(funcName);
				Iterator<FunctionPoint> iter=points.iterator();
				
				while(iter.hasNext()){
					System.out.println(funcName + iter.next().getTime());
				}
			}
	*/	}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void createFuncNameToFuncStateMap(){
		Set<String> funcNames=funcNameToFuncPointMap.keySet();
		Iterator<String> iter=funcNames.iterator();
		while(iter.hasNext()){
			String funcName=iter.next();
			List<FunctionPoint> funcPoints=new ArrayList<FunctionPoint>((List<FunctionPoint>) funcNameToFuncPointMap.get(funcName));
			int i=0;
			while(i<funcPoints.size()){
				FunctionState funcState;
				FunctionPoint entry=null;
				FunctionPoint exit=null;
				FunctionPoint funcPoint=funcPoints.get(i);
				String pointName=funcPoint.getPointName();
				if(pointName.toLowerCase().equals("enter")){
					if(funcPoints.size()==1){
						funcPoints.remove(entry);
						i=0;
						break;
					}
					if(funcPoints.get(i+1).getPointName().toLowerCase().equals("enter")){
					
						for(int count=i+1;count<funcPoints.size();count++){
							
							if(funcPoints.get(count).getPointName().toLowerCase().equals("exit")){
							
									entry=funcPoints.get(count-1);
									exit=funcPoints.get(count);
									funcPoints.remove(entry);
									funcPoints.remove(exit);
									i=0;
									break;							
								
							}
						}
					}
					else{
						entry=funcPoint;
						for(int j=i;j<funcPoints.size();j++){
							FunctionPoint point=funcPoints.get(j);
							if(point.getPointName().toLowerCase().equals("exit")){
								exit=point;
								funcPoints.remove(entry);
								funcPoints.remove(exit);
								i=0;
								break;
							}
							
						}
					}
					funcState=new FunctionState(entry, exit);
					List<FunctionState> fStates=(List<FunctionState>) funcNameToFuncStateMap.get(funcName);
					
					if(entry!=null && exit==null){
						
						exit=new FunctionPoint("exit", entry.getVariables(), entry.getDomHtml(), entry.getTime(), entry.getFunctionBranchCoverage());
						funcPoints.remove(entry);
						i=0;
					}
					if(entry==null && exit==null)
						break;
					if(!fStates.contains(funcState) && entry!=null && exit!=null)
						funcNameToFuncStateMap.put(funcName, funcState);
				}
				else{
					entry=funcPoint;
					exit=funcPoints.get(i-1);
					funcPoints.remove(entry);
					funcPoints.remove(exit);
					i=0;
					funcState=new FunctionState(entry, exit);
					List<FunctionState> fStates=(List<FunctionState>) funcNameToFuncStateMap.get(funcName);
					if(!fStates.contains(funcState)  && entry!=null && exit!=null)
						funcNameToFuncStateMap.put(funcName, funcState);
				}
			}
		}
		
		
		Set<String> keys=funcNameToFuncStateMap.keySet();
		Iterator<String> funcIter=keys.iterator();
		while(funcIter.hasNext()){
			String funcName=iter.next();
			ArrayList<FunctionState> funcStates=new ArrayList<FunctionState>(funcNameToFuncStateMap.get(funcName));
			List<FunctionState> desiredFuncStates=getAbstractedListOfState(funcStates);
			for(FunctionState fs:funcStates){
				if(!desiredFuncStates.contains(fs)){
					funcNameToFuncStateMap.get(funcName).remove(fs);
				}
			}
		}
	
	
	}
	

	
	private void createFuncEntryToFuncExitMap(){
		
		Set<String> keys=funcNameToFuncStateMap.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			String funcName=iter.next();
			ArrayList<FunctionState> funcStates=new ArrayList<FunctionState>(funcNameToFuncStateMap.get(funcName));
		//	ArrayList<FunctionState> funcStates=(ArrayList<FunctionState>) ((ArrayList<FunctionState>) funcStatestemp).clone();
			
			ArrayListMultimap<FunctionPoint,FunctionPoint> funcPointMltimap=ArrayListMultimap.create();
			Iterator<FunctionState> fStOuterIter=funcStates.iterator();
			while(fStOuterIter.hasNext()){
				
				FunctionState funcState=fStOuterIter.next();
				FunctionPoint funcEntry=funcState.getFunctionEntry();
				FunctionPoint funcExit=funcState.getFunctionExit();
				ArrayList<Variable> varList=funcEntry.getVariables();
				funcPointMltimap.put(funcEntry, funcExit);
				funcStates.remove(funcState);
				Iterator<FunctionState> fStIter=funcStates.iterator();
				while(fStIter.hasNext()){
					FunctionState fState=fStIter.next();
					FunctionPoint nextFuncEntry=fState.getFunctionEntry();
					ArrayList<Variable> nextVarList=nextFuncEntry.getVariables();
					
					if(nextVarList.equals(varList)){
						FunctionState nextFuncState=fState;
						FunctionPoint nextFuncExit=nextFuncState.getFunctionExit();
						List<FunctionPoint> exitPointList=funcPointMltimap.get(funcEntry);
						boolean similar=false;
						for(int count=0;count<exitPointList.size();count++){
							if(functionPointsSimilar(exitPointList.get(count),nextFuncExit)){
								similar=true;
								break;
							
							}
						}
						if(!similar){
							
							funcPointMltimap.put(funcEntry, nextFuncExit);
							
						}
						funcStates.remove(fState);
						fStIter=funcStates.iterator();
						
					}
					
		/*			if(nextVarList.size()==varList.size()){
						for(int k=0;k<nextVarList.size();k++){
							if(!nextVarList.get(k).equals(varList.get(k))){
								equal=false;
								break;
							}
						}
					}
		*/			
					
				}
				fStOuterIter=funcStates.iterator();
				
			}
			funcEntryPointToExitPointMap.put(funcName, funcPointMltimap);
			
		}
	}
	
	
	
	
	private boolean functionPointsSimilar(FunctionPoint funcPoint1, FunctionPoint funcPoint2){
		if(funcPoint1.getPointName().equals(funcPoint2.getPointName())){
			ArrayList<Variable> varList1=funcPoint1.getVariables();
			ArrayList<Variable> varList2=funcPoint2.getVariables();
			if(varList1.equals(varList2))
				return true;
		}
		return false;
	}
	
	public static void makeFunctionListOfOriginalVersion(){
		Set<String> keys=funcNameToFuncStateMap.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			functionListOfOriginalVersion.add(iter.next());
		}
	
	}
	
	
	private List<FunctionState> getAbstractedListOfState(ArrayList<FunctionState> funcStates){
		List<ArrayList<FunctionState>> listOfSetOfStates_DOM_RetType=new ArrayList<ArrayList<FunctionState>>();
		List<ArrayList<FunctionState>> listOfSetOfStates_BrCovg=new ArrayList<ArrayList<FunctionState>>();

		List<Integer> addedItemsIndex_DOM_RetType=new ArrayList<Integer>();
		List<Integer> addedItemsIndex_BrCovg=new ArrayList<Integer>();
		
		for(int i=0;i<funcStates.size();i++){
			FunctionState f_i=funcStates.get(i);
			if(!addedItemsIndex_DOM_RetType.contains(i)){
				
				ArrayList<FunctionState> stateSet_DOM_Ret=new ArrayList<FunctionState>();
				stateSet_DOM_Ret.add(f_i);
				addedItemsIndex_DOM_RetType.add(i);
				for(int j=i+1;j<funcStates.size();j++){
					
					FunctionState f_j=funcStates.get(j);
					if(f_j.similarState_DOM_RetType(f_i)){
						stateSet_DOM_Ret.add(f_j);
						addedItemsIndex_DOM_RetType.add(j);
					}
				}
				listOfSetOfStates_DOM_RetType.add(stateSet_DOM_Ret);
			}
			if(!addedItemsIndex_BrCovg.contains(i)){
					
					ArrayList<FunctionState> stateSet_BrCovg=new ArrayList<FunctionState>();
					stateSet_BrCovg.add(f_i);
					addedItemsIndex_BrCovg.add(i);
					for(int j=i+1;j<funcStates.size();j++){
					
						FunctionState f_j=funcStates.get(j);
						if(f_j.sameBranchCoverage(f_i)){
							stateSet_BrCovg.add(f_j);
							addedItemsIndex_BrCovg.add(j);
						}
					}
					listOfSetOfStates_BrCovg.add(stateSet_BrCovg);
				}
			}
			return setCoveringAlgo(listOfSetOfStates_DOM_RetType, listOfSetOfStates_BrCovg);
		
		
		
	}


	private List<FunctionState> setCoveringAlgo(List<ArrayList<FunctionState>> listOfSetOfStates_DOM_RetType,
			List<ArrayList<FunctionState>> listOfSetOfStates_BrCovg){
	
		List<FunctionState> allSelectedFunctioStates=new ArrayList<FunctionState>();
		List<Integer> indexOfRemovedSets_DOM_RetType=new ArrayList<Integer>();
		List<Integer> indexOfRemovedSets_BrCovg=new ArrayList<Integer>();
		int totalsize=listOfSetOfStates_DOM_RetType.size()+listOfSetOfStates_BrCovg.size();
		Random rand=new Random(10);
		int randIndex=rand.nextInt(totalsize);
		List<FunctionState> funcStList_BrCovg;
		List<FunctionState> funcStList_DOM_RetType;
		FunctionState selectedState;
		boolean coverageStateSets=false;
		if(randIndex>=listOfSetOfStates_DOM_RetType.size()){
			randIndex=randIndex-listOfSetOfStates_DOM_RetType.size();
			funcStList_BrCovg=listOfSetOfStates_BrCovg.get(randIndex);
			selectedState=funcStList_BrCovg.get(rand.nextInt(funcStList_BrCovg.size()));
			indexOfRemovedSets_BrCovg.add(randIndex);
			listOfSetOfStates_BrCovg.remove(randIndex);
			coverageStateSets=true;
			
		}
		else{
			funcStList_DOM_RetType=listOfSetOfStates_DOM_RetType.get(randIndex);		
			selectedState=funcStList_DOM_RetType.get(rand.nextInt(funcStList_DOM_RetType.size()));
			indexOfRemovedSets_DOM_RetType.add(randIndex);
			listOfSetOfStates_DOM_RetType.remove(randIndex);
			coverageStateSets=false;
		}
		
		
		allSelectedFunctioStates.add(selectedState);
		
		if(coverageStateSets){
			boolean setDeleted=false;
			for(int i=0;i<listOfSetOfStates_DOM_RetType.size();i++){
				setDeleted=false;
				ArrayList<FunctionState> fs=listOfSetOfStates_DOM_RetType.get(i);
				for(int j=0;j<fs.size();j++){
					if(fs.get(j).equals(selectedState)){
						listOfSetOfStates_DOM_RetType.remove(i);
						setDeleted=true;
						break;
					}
				}
				if(setDeleted)
					break;
				
			}
		}
		else{
				
			boolean setDeleted=false;
			for(int i=0;i<listOfSetOfStates_BrCovg.size();i++){
				setDeleted=false;
				ArrayList<FunctionState> fs=listOfSetOfStates_BrCovg.get(i);
				for(int j=0;j<fs.size();j++){
					if(fs.get(j).equals(selectedState)){
						listOfSetOfStates_BrCovg.remove(i);
						setDeleted=true;
						break;
					}
				}
				if(setDeleted)
					break;
				
			}
				
		}
			
			
		while(listOfSetOfStates_BrCovg.size()!=0 || listOfSetOfStates_DOM_RetType.size()!=0){
			boolean stateSelected=false;
			for(int i=0;i<listOfSetOfStates_BrCovg.size();i++){
					
				ArrayList<FunctionState> fsList=listOfSetOfStates_BrCovg.get(i);
				for(int j=0;j<fsList.size();j++){
					FunctionState fs=fsList.get(j);
					for(int count=0;count<listOfSetOfStates_DOM_RetType.size();count++){
						if(listOfSetOfStates_DOM_RetType.get(count).contains(fs)){
							
							allSelectedFunctioStates.add(fs);
							stateSelected=true;
							coverageStateSets=true;
							listOfSetOfStates_DOM_RetType.remove(count);
							listOfSetOfStates_BrCovg.remove(i);
							break;
							
						}
					}
					if(stateSelected)
						break;
						
				}
				if(stateSelected)
					break;
			}
			if(!stateSelected){
				for(int i=0;i<listOfSetOfStates_DOM_RetType.size();i++){
					ArrayList<FunctionState> fsList=listOfSetOfStates_DOM_RetType.get(i);
					for(int j=0;j<fsList.size();j++){
						FunctionState fs=fsList.get(j);
						for(int count=0;count<listOfSetOfStates_BrCovg.size();count++){
							if(listOfSetOfStates_BrCovg.get(count).contains(fs)){
								
								allSelectedFunctioStates.add(fs);
								stateSelected=true;
								coverageStateSets=false;
								listOfSetOfStates_BrCovg.remove(count);
								listOfSetOfStates_DOM_RetType.remove(i);
								break;
							}
						}
						if(stateSelected)
							break;
							
					}
					if(stateSelected)
						break;
				}
				
				
			}
			if(listOfSetOfStates_BrCovg.size()!=0 && !stateSelected){
				int randomIndex_br=rand.nextInt(listOfSetOfStates_BrCovg.size());
				ArrayList <FunctionState> listOfStates=listOfSetOfStates_BrCovg.get(randomIndex_br);
				FunctionState fs=listOfStates.get(rand.nextInt(listOfStates.size()));
				allSelectedFunctioStates.add(fs);
				stateSelected=true;
				coverageStateSets=true;
				listOfSetOfStates_BrCovg.remove(randomIndex_br);
		
			
			}
			else
				if(listOfSetOfStates_DOM_RetType.size()!=0 && !stateSelected){
					int randomIndex_domRetType=rand.nextInt(listOfSetOfStates_DOM_RetType.size());
					ArrayList <FunctionState> listOfStates=listOfSetOfStates_DOM_RetType.get(randomIndex_domRetType);
					FunctionState fs=listOfStates.get(rand.nextInt(listOfStates.size()));
					allSelectedFunctioStates.add(fs);
					stateSelected=true;
					coverageStateSets=false;
					listOfSetOfStates_DOM_RetType.remove(randomIndex_domRetType);
			
				
				}
			
		}
		
			
		return allSelectedFunctioStates;
		
	}
	@Override
	protected void functionStateAbstraction() {
		// TODO Auto-generated method stub
		
	}
	
/*	private void addingDomRelatedFunctionPoint(AccessedDOMNode accessedDomNode, String funcName, String pointName, long time){
		List<FunctionPoint> currentFuncNameToFuncPointMap=(List<FunctionPoint>) funcNameToFuncPointMap.get(funcName);
		if(currentFuncNameToFuncPointMap!=null){
			java.util.Collections.sort(currentFuncNameToFuncPointMap, vc);
			FunctionPoint lastPoint=currentFuncNameToFuncPointMap.get(currentFuncNameToFuncPointMap.size()-1);
			if(lastPoint.getPointName().equals("enter")){
				lastPoint.addAccessedDomNode(accessedDomNode);
			}
			else{
				FunctionPoint functionPoint=new FunctionPoint(pointName, accessedDomNode, time);
				funcNameToFuncPointMap.put(funcName, functionPoint);
			}
		}
		else{
			FunctionPoint functionPoint=new FunctionPoint(pointName, accessedDomNode, time);
			funcNameToFuncPointMap.put(funcName, functionPoint);
		}
	}
	
	private void addingNonDomRelatedFunctionPoint(ArrayList<Variable> variables, String funcName, String pointName, long time){
		List<FunctionPoint> currentFuncNameToFuncPointMap=(List<FunctionPoint>) funcNameToFuncPointMap.get(funcName);
		if(currentFuncNameToFuncPointMap!=null){
			java.util.Collections.sort(currentFuncNameToFuncPointMap, vc);
			FunctionPoint lastPoint=currentFuncNameToFuncPointMap.get(currentFuncNameToFuncPointMap.size()-1);
			if(lastPoint.getPointName().equals("enter")
					&& lastPoint.getAccessedDomNodes().size()>0
					&& lastPoint.getVariables().size()==0){
				lastPoint.addVariable(variables);
			}
			else{
				FunctionPoint functionPoint=new FunctionPoint(pointName, variables, time);
				funcNameToFuncPointMap.put(funcName, functionPoint);
			}
		}
		else{
			FunctionPoint functionPoint=new FunctionPoint(pointName, variables, time);
			funcNameToFuncPointMap.put(funcName, functionPoint);
		}
	}
*/
}
