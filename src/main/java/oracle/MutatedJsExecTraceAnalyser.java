package oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import executionTracer.JSExecutionTracer;

public class MutatedJsExecTraceAnalyser extends JsExecTraceAnalyser{

	
	public static Multimap<String, FunctionState> funcNameToFuncStateMap_modifiedVer=ArrayListMultimap.create();

	private Multimap<String, FunctionPoint> funcNameToFuncPointMap=ArrayListMultimap.create();
	public MutatedJsExecTraceAnalyser(String outputFolder) {
		super(outputFolder);
		
	}
	
	@Override
	protected List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(outputFolder +  JSExecutionTracer.MUTATEDEXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".txt")) {
				result.add(outputFolder + JSExecutionTracer.MUTATEDEXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}
	
	@Override
	protected void startAnalysingJsExecTraceFiles(){
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
						else if(line.contains("dom::")){
							domHtml=line.split("::")[1];
						}
						else if(line.contains("node::")){
							String node=line.split("::")[1];
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

					
				functionPoint=new FunctionPoint(pointName, variables, domHtml, time);
				if(domNodes.size()>0){
					functionPoint.addAccessedDomNodes(domNodes);
				}
				funcNameToFuncPointMap.put(funcName, functionPoint);
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
	
	@Override
	protected void createFuncNameToFuncStateMap(){
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
					funcNameToFuncStateMap_modifiedVer.put(funcName, funcState);
				}
			}
		}
	}
	


}
