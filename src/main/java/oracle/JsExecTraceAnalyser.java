package oracle;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.crawljax.util.Helper;
import executionTracer.JSExecutionTracer;

public abstract class JsExecTraceAnalyser {
	
	/**
	 * (functioni --> (entryi --> (exiti)))
	 * 
	 * ArrayList --> (variable, type, value)
	 */
	
//	protected Multimap<String, FunctionState> funcNameToFuncStateMap=ArrayListMultimap.create();


	
//	protected Multimap<String, FunctionPoint> funcNameToFuncPointMap;
		   
	
	
	protected String outputFolder;
	protected List<String> traceFilenameAndPath;
	
	protected Comparator<FunctionPoint> vc;
	protected Comparator<Object[]> funcNameFuncPointComp;
	
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
		
		
		funcNameFuncPointComp=new Comparator<Object[]>() {

			@Override
			public int compare(Object[] funcNameFuncPoint1, Object[] funcNameFuncPoint2) {
		        
				FunctionPoint f1=(FunctionPoint) funcNameFuncPoint1[0];
				FunctionPoint f2=(FunctionPoint) funcNameFuncPoint2[0];
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
		
//		funcNameToFuncPointMap = ArrayListMultimap.create();
		
		startAnalysingJsExecTraceFiles();
		createFuncNameToFuncStateMap();
	

	}
	
	public List<String> getTraceFilenameAndPath() {
		return traceFilenameAndPath;
	}
	
/*	public Multimap<String, FunctionState> getFuncNameToFuncStateMap(){
		return funcNameToFuncStateMap;
	}
*/	

	
	protected abstract void startAnalysingJsExecTraceFiles();
/*	{
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
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
*/	
	protected abstract List<String> allTraceFiles(); 
/*	{
		ArrayList<String> result = new ArrayList<String>();

		// find all trace files in the trace directory
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
*/	
	
	protected abstract void createFuncNameToFuncStateMap();
/*	{
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
	
*/
	
	
	
	protected abstract void functionStateAbstraction();
	

}


