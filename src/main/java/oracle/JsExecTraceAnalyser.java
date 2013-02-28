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

public class JsExecTraceAnalyser {
	
	/**
	 * (functioni --> (entryi --> (exiti)))
	 * 
	 * ArrayList --> (variable, type, value)
	 */
	
	ListMultimap<String, ListMultimap<ArrayList<String>, ArrayList<String>>> functionEntryExitMap=
			ArrayListMultimap.create();


	
	Multimap<String, FunctionPoint> funcNameToFuncPointMap;
		   
	
	
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

	}
	
	public List<String> getTraceFilenameAndPath() {
		return traceFilenameAndPath;
	}
	
	public Multimap<String, FunctionPoint> getFuncNameToFuncPointMap(){
		return funcNameToFuncPointMap;
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
						
						if(variableName!="" && value!="" && type!=""){
							varibale=new Variable(variableName, value, type);
							variables.add(varibale);
							variableName="";
							value="";
							type="";
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
	
	

}


