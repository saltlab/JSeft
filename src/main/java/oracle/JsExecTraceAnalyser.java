package oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.crawljax.util.Helper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import executionTracer.JSExecutionTracer;
import executionTracer.ProgramPoint;

public class JsExecTraceAnalyser {
	
	/**
	 * (functioni --> (entryi --> (exiti)))
	 * 
	 * ArrayList --> (variable, type, value)
	 */
	
	private Multimap<String, FunctionState> funcNameToFuncStateMap=ArrayListMultimap.create();
	private Multimap<String, Multimap<FunctionPoint,FunctionPoint>> funcEntryPointToExitPointMap=ArrayListMultimap.create();

	
	private Multimap<String, FunctionPoint> funcNameToFuncPointMap;
		   
	
	
	private String outputFolder;
	private List<String> traceFilenameAndPath;
	
	private Comparator<FunctionPoint> vc;
	
	public JsExecTraceAnalyser(String outputFolder){
		vc=new Comparator<FunctionPoint>() {

			@Override
			public int compare(FunctionPoint f1, FunctionPoint f2) {
		        if(f1.getTime()>f2.getTime())
		        	return 1;
		        else if(f1.getTime()<f2.getTime()){
		        	return -1;
		        }
		        return 0;
				
			}
		};
		this.outputFolder=Helper.addFolderSlashIfNeeded(outputFolder);
		traceFilenameAndPath=allTraceFiles();
		
		funcNameToFuncPointMap = ArrayListMultimap.create();
		
		startAnalysingJsExecTraceFiles();
		createFuncNameToFuncStateMap();
		createFuncEntryToFuncExitMap();

	}
	
	public List<String> getTraceFilenameAndPath() {
		return traceFilenameAndPath;
	}
	
	public Multimap<String, FunctionState> getFuncNameToFuncStateMap(){
		return funcNameToFuncStateMap;
	}
	
	public Multimap<String, Multimap<FunctionPoint, FunctionPoint>> getFuncEntryPointToExitPointMap(){
		return funcEntryPointToExitPointMap;
	}
	
	private void startAnalysingJsExecTraceFiles(){
		try{
			List<String>filenameAndPathList=getTraceFilenameAndPath();
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
					Variable varibale;

					ArrayList<Variable> variables=new ArrayList<Variable>();
					FunctionPoint functionPoint;
				
					while (!(line = input.readLine()).equals
							("===========================================================================")){

						if(line.contains("time::")){
						
							time=Long.valueOf(line.split("::")[1]);
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
						
						if(variableName!="" && value!="" && type!="" && variableUsage!=""){
							varibale=new Variable(variableName, value, type, variableUsage);
							variables.add(varibale);
							variableName="";
							value="";
							type="";
							variableUsage="";
							
							
						}
						
					
					}
					
					functionPoint=new FunctionPoint(pointName, variables, time);
					funcNameToFuncPointMap.put(funcName, functionPoint);
		//			List<FunctionPoint> functionPoints=(List<FunctionPoint>) funcNameToFuncPointMap.get(funcName);
		//			java.util.Collections.sort(functionPoints, bvc);

				}
				input.close();
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
	
	
	private List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(outputFolder +  JSExecutionTracer.EXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".txt")) {
				result.add(outputFolder + JSExecutionTracer.EXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}
	
	private void createFuncNameToFuncStateMap(){
		Set<String> funcNames=funcNameToFuncPointMap.keySet();
		Iterator<String> iter=funcNames.iterator();
		while(iter.hasNext()){
			String funcName=iter.next();
			List<FunctionPoint> funcPoints=(List<FunctionPoint>) funcNameToFuncPointMap.get(funcName);
			for(int i=0;i<funcPoints.size();i++){
				FunctionState funcState;
				FunctionPoint entry=null;
				FunctionPoint exit=null;
				FunctionPoint funcPoint=funcPoints.get(i);
				String pointName=funcPoint.getPointName();
				if(pointName.toLowerCase().equals("enter")){
					entry=funcPoint;
					for(int j=i+1;j<funcPoints.size();j++){
						FunctionPoint point=funcPoints.get(j);
						if(point.getPointName().toLowerCase().equals("exit")){
							exit=point;
							break;
						}
						
					}
					funcState=new FunctionState(entry, exit);
					funcNameToFuncStateMap.put(funcName, funcState);
				}
			}
		}
	}
	
	private void createFuncEntryToFuncExitMap(){
		Set<String> keys=funcNameToFuncStateMap.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			String funcName=iter.next();
			List<FunctionState> funcStates=(List<FunctionState>) funcNameToFuncStateMap.get(funcName);
			Multimap<FunctionPoint,FunctionPoint> funcPointMltimap=ArrayListMultimap.create();
			for(int i=0;i<funcStates.size();i++){
			
				FunctionState funcState=funcStates.get(i);
				FunctionPoint funcEntry=funcState.getFunctionEntry();
				FunctionPoint funcExit=funcState.getFunctionExit();
				ArrayList<Variable> varList=funcEntry.getVariables();
				funcPointMltimap.put(funcEntry, funcExit);
				
				for(int j=i+1;j<funcStates.size();j++){
					FunctionPoint nextFuncEntry=funcStates.get(j).getFunctionEntry();
					ArrayList<Variable> nextVarList=nextFuncEntry.getVariables();
					boolean equal=true;
					if(nextVarList.size()==varList.size()){
						for(int k=0;k<nextVarList.size();k++){
							if(!nextVarList.get(k).equals(varList.get(k))){
								equal=false;
								break;
							}
						}
					}
					if(equal){
						FunctionPoint nextFuncExit=funcStates.get(j).getFunctionExit();
						funcPointMltimap.put(funcEntry, nextFuncExit);
					}
					
				}
				
			}
			funcEntryPointToExitPointMap.put(funcName, funcPointMltimap);
			
		}
	}
	
	

}


