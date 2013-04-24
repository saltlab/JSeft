package domMutation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.crawljax.util.Helper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import executionTracer.DOMMuteExecutionTracer;

public abstract class Dom_OrigMut_Analyser {
	/*<clickedOn_stateName_xpath->attribute1, attribute2,...>*/
//	protected static ArrayListMultimap<String, DomAttribute> stateXpathToNodeAttrsMap=ArrayListMultimap.create();
	protected String outputFolder;
	private List<String> traceFilenameAndPath;
	
	public Dom_OrigMut_Analyser(String outputFolder){
	
		this.outputFolder=Helper.addFolderSlashIfNeeded(outputFolder);
		traceFilenameAndPath=allTraceFiles();
		startReadingDomTraceFiles();
		
	}
	
	protected abstract List<String> allTraceFiles();
/*	{
		ArrayList<String> result = new ArrayList<String>();

		// find all trace files in the trace directory
		File dir = new File(outputFolder +  DOMMuteExecutionTracer.EXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".txt")) {
				result.add(outputFolder + DOMMuteExecutionTracer.EXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}
*/	
/*	public ArrayListMultimap<String, DomAttribute> getstateXpathToNodeAttrsMap(){
		return stateXpathToNodeAttrsMap;
	}
*/	
	public List<String> getTraceFilenameAndPath() {
		return traceFilenameAndPath;
	}
	
	protected abstract void startReadingDomTraceFiles();
/*	{
		try{
			List<String>filenameAndPathList=getTraceFilenameAndPath();
			for (String filenameAndPath:filenameAndPathList){
				BufferedReader input =
					new BufferedReader(new FileReader(filenameAndPath));
			
				String clickedOn_state_xpath="", inputline="";
				inputline=input.readLine();
				String clickedOn=inputline.split("::")[1];
				inputline=input.readLine();
				String stateName=inputline.split("::")[1];
				input.readLine();
				while ((inputline = input.readLine()) != null){
			
				if ("".equals(inputline))
					break;
				
				String xpath="";
				String tagName=inputline.split("::")[1];
				DomAttribute attr=new DomAttribute("tagName", tagName);
					while (!(inputline = input.readLine()).equals("===========================================================================")){

						if(inputline.contains("xpath::")){
							xpath=inputline.split("::")[1];
							clickedOn_state_xpath=clickedOn + "::" + stateName + "::" + xpath;
							boolean repeatedAttr=isAttributeRepeated(clickedOn_state_xpath, attr);
							if(!repeatedAttr)
								stateXpathToNodeAttrsMap.put(clickedOn_state_xpath, attr);
						}
						else {
							String attrName=inputline.split("::")[0];
							String attrValue=inputline.split("::")[1];
							DomAttribute domAttr=new DomAttribute(attrName, attrValue);
							boolean repeatedAttr=isAttributeRepeated(clickedOn_state_xpath, domAttr);
							if(!repeatedAttr)
								stateXpathToNodeAttrsMap.put(clickedOn_state_xpath, domAttr);
						}
					}
	
				
		
				}
			  
			 input.close();
			}
		
		
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
*/	
	
/*	private boolean isAttributeRepeated(String clickedOn_state_xpath, DomAttribute domAttr){
		boolean repeatedAttr=false;
		List<DomAttribute> attrList=stateXpathToNodeAttrsMap.get(clickedOn_state_xpath);
		if(attrList!=null){
			for(DomAttribute attribute:attrList){
				if(attribute.equals(domAttr)){
					repeatedAttr=true;
					break;
				}
			}
		}
		return repeatedAttr;
	}
*/	
	
	
	
	
}


