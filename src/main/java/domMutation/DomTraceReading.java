package domMutation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import com.crawljax.util.Helper;
import executionTracer.DOMExecutionTracer;

public class DomTraceReading {

	private TreeMap<String, ArrayList<NodeProperty>> func_domNode_map=new TreeMap<String, ArrayList<NodeProperty>>();
	private String outputFolder;
	private List<String> traceFilenameAndPath;
	
	public DomTraceReading (String outputFolder){
	
		this.outputFolder=Helper.addFolderSlashIfNeeded(outputFolder);
		traceFilenameAndPath=allTraceFiles();
		startReadingDomTraceFiles();
		
	}
	
	private List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(outputFolder +  DOMExecutionTracer.EXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".txt")) {
				result.add(outputFolder + DOMExecutionTracer.EXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}
	
	public TreeMap<String, ArrayList<NodeProperty>> getFunc_domNode_map(){
		return func_domNode_map;
	}
	
	public List<String> getTraceFilenameAndPath() {
		return traceFilenameAndPath;
	}
	
	private void startReadingDomTraceFiles(){
		try{
			List<String>filenameAndPathList=getTraceFilenameAndPath();
			for (String filenameAndPath:filenameAndPathList){
				BufferedReader input =
					new BufferedReader(new FileReader(filenameAndPath));
				
				String node="", line="", inputline="",value="", funcName="";
			  while ((inputline = input.readLine()) != null){
			
				if ("".equals(inputline))
					break;
				String[] str=inputline.split(":::");
				str=str[0].split("\\.");
				funcName=str[str.length-1];
				while (!(inputline = input.readLine()).equals("===========================================================================")){
					if(inputline.contains("node::")){
						node=inputline.replace("node::", "");
					}
					else if(inputline.contains("line::")){
						line=inputline.replace("line::", "");
					}
					else if(inputline.contains("value::")){
						value=inputline.replace("value::", "");
					}
				}
				ArrayList<NodeProperty> nodeProps=func_domNode_map.get(funcName);
				if(nodeProps!=null){
					NodeProperty nodeProp=new NodeProperty(node, line, value);
					nodeProps.add(nodeProp);
				}
				else{
					NodeProperty nodeProp=new NodeProperty(node, line, value);
					ArrayList<NodeProperty> arrayListNodeProp=new ArrayList<NodeProperty>();
					arrayListNodeProp.add(nodeProp);
					func_domNode_map.put(funcName, arrayListNodeProp);
				}
				
		
			  }
			  
			 
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
