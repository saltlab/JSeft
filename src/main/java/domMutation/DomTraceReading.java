package domMutation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.codehaus.jackson.map.ObjectMapper;

import com.crawljax.util.Helper;
import executionTracer.DOMExecutionTracer;

public class DomTraceReading {
	/* <stateName-functionName, [domNode, line, value], [domNode, line, value] ....> */
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
				
				Node domNode = null;
				String line="", inputline="",value="", funcName="";
				inputline=input.readLine();
				String stateName=inputline.replace("state::", "");
				input.readLine();
				while ((inputline = input.readLine()) != null){
			
				if ("".equals(inputline))
					break;
				
				
				String[] str=inputline.split(":::");
				str=str[0].split("\\.");
				funcName=str[str.length-1];
				while (!(inputline = input.readLine()).equals("===========================================================================")){
					if(inputline.contains("node::")){
						String node=inputline.replace("node::", "");
						ObjectMapper mapper = new ObjectMapper();  
					    domNode = mapper.readValue(node, Node.class);  
					    mapper.writeValueAsString(domNode);  
/*						String[] nodeArray=inputline.replace("node::", "")
									.replace("{", "")
									.replace("}", "").split(":");
						node=nodeArray[nodeArray.length-1].replaceFirst("\"", "");
						node=node.substring(0, node.lastIndexOf("\""));
*/						
					}
					else if(inputline.contains("line::")){
						line=inputline.replace("line::", "");
					}
					else if(inputline.contains("value::")){
						value=inputline.replace("value::", "");
					}
				}
				ArrayList<NodeProperty> nodeProps=func_domNode_map.get(stateName + "-" + funcName);
				if(nodeProps!=null){
					NodeProperty nodeProp=new NodeProperty(domNode, line, value);
					nodeProps.add(nodeProp);
				}
				else{
					NodeProperty nodeProp=new NodeProperty(domNode, line, value);
					ArrayList<NodeProperty> arrayListNodeProp=new ArrayList<NodeProperty>();
					arrayListNodeProp.add(nodeProp);
					func_domNode_map.put(stateName + "-" + funcName, arrayListNodeProp);
				}
				
		
			  }
			  
			 input.close();
			}
		
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<NodeProperty> getAllNodes(){
		Set<String> keys=func_domNode_map.keySet();
		Iterator<String> it=keys.iterator();
		ArrayList<NodeProperty> nodeProps=new ArrayList<NodeProperty>();
		while(it.hasNext()){
			String stateNameFuncName=it.next();
			ArrayList<NodeProperty> nodePropList=func_domNode_map.get(stateNameFuncName);
			nodeProps.addAll(nodePropList);
		}
		
		return nodeProps;
	}
	
}
