package domMutation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;

import executionTracer.DOMExecutionTracer;

public class Dom_Orig_Analyser extends Dom_OrigMut_Analyser {
	
	public static ArrayListMultimap<String, DomAttribute> stateXpathToNodeAttrsMap=ArrayListMultimap.create();

	public Dom_Orig_Analyser(String outputFolder) {
		super(outputFolder);
		
	}


	@Override
	protected List<String> allTraceFiles() {
		
		ArrayList<String> result = new ArrayList<String>();

		// find all trace files in the trace directory
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

	@Override
	protected void startReadingDomTraceFiles() {

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
	
	private boolean isAttributeRepeated(String clickedOn_state_xpath, DomAttribute domAttr){
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

}
