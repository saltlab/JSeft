package oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.crawljax.util.Helper;
import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.io.Files;

import domMutation.DomAttribute;
import domMutation.Dom_Mut_Analyser;
import domMutation.Dom_Orig_Analyser;

public class DomStateComparator {

	/**
	 *  (clickedOn::state->(elementXpath->attibutes))
	 */
	private ArrayListMultimap<String, ArrayListMultimap<String, DomAttribute>> domOracleMultimap=ArrayListMultimap.create();
	private String outputFolder;
	private String fileName="domOracle.txt";
	
	public DomStateComparator(String outputFolder){
		this.outputFolder=outputFolder;
	}
	public ArrayListMultimap<String, ArrayListMultimap<String, DomAttribute>> getDomOracleMultimap(){
		return domOracleMultimap;
	}
	
	public void analysingOutPutDiffs() throws IOException{
		
		Set<String> keys=Dom_Mut_Analyser.stateXpathToNodeAttrsMap_MutVer.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			String clickedOn_state_xpath=iter.next();
			String element=clickedOn_state_xpath.split("::")[2];
			String state=clickedOn_state_xpath.split("::")[1];
			String clickedOn=clickedOn_state_xpath.split("::")[0];
			String clickedOn_state=clickedOn + "::" + state;
			List<DomAttribute> mutDomAttrs=Dom_Mut_Analyser.stateXpathToNodeAttrsMap_MutVer.get(clickedOn_state_xpath);
			List<DomAttribute> origDomAttrs=Dom_Orig_Analyser.stateXpathToNodeAttrsMap.get(clickedOn_state_xpath);
			if(origDomAttrs!=null){
				for(int i=0;i<origDomAttrs.size();i++){
					boolean matched=false;
					DomAttribute origDomAttr=mutDomAttrs.get(i);
				
					for(int j=0;j<mutDomAttrs.size();j++){
						if(origDomAttr.equals(mutDomAttrs.get(j))){
							matched=true;
							break;
						}
						
					}
				
					if(!matched){
					
						addElementToDomOracleMultimap(element, origDomAttr, clickedOn_state);
				
					}
				}	
			}
			else{
				
				ArrayListMultimap<String, DomAttribute> newElem=ArrayListMultimap.create();
				DomAttribute elemNotExit=new DomAttribute("ElementNotExist", "ElementNotExist");
				newElem.put(element, elemNotExit);	
				domOracleMultimap.put(clickedOn_state, newElem);
				
			}
		}
		
		writeDomOracleMultimapToFile();
		
		
	}
	
	private void addElementToDomOracleMultimap(String element, DomAttribute origDomAttr, String clickedOn_state){
		List<ArrayListMultimap<String, DomAttribute>> elemList=domOracleMultimap.get(clickedOn_state);
		if(elemList!=null){
			for(ArrayListMultimap<String, DomAttribute> elem:elemList){
				
				if(elem.get(element)!=null){
					
					elem.put(element, origDomAttr);
					
				}
				
				else{
					
					ArrayListMultimap<String, DomAttribute> newElem=ArrayListMultimap.create();
					newElem.put(element, origDomAttr);	
					domOracleMultimap.put(clickedOn_state, newElem);
					
				}
			}
		}
		else{
			ArrayListMultimap<String, DomAttribute> newElem=ArrayListMultimap.create();
			newElem.put(element, origDomAttr);	
			domOracleMultimap.put(clickedOn_state, newElem);
		}
		
	}
	
	private String getDomOracleMultimapAsString(){
		StringBuffer result=new StringBuffer();
		Set<String> keys=domOracleMultimap.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			String clickedOn_state=iter.next();
			result.append("clickedOn_state::" + clickedOn_state + "\n");
			List<ArrayListMultimap<String, DomAttribute>> elements=domOracleMultimap.get(clickedOn_state);
			for(ArrayListMultimap<String, DomAttribute> elementMap:elements){
				Set<String> keySet=elementMap.keySet();
				Iterator<String> it=keySet.iterator();
				while(it.hasNext()){
					String elemXpath=it.next();
					result.append("elementXpath::" + elemXpath + "\n");
					List<DomAttribute> domAttrs=elementMap.get(elemXpath);
					for(DomAttribute domAttr:domAttrs){
						String attrName=domAttr.getAttributeName();
						String attrValue=domAttr.getAttributeValue();
						result.append("attribute::" + attrName + "::" + attrValue + "\n");
						
					}
				}
			}
			result.append("===========================================================================" +"\n");
		}
		
		return result.toString();
	}
	
	private void writeDomOracleMultimapToFile() throws IOException{
		
		String domOracleMap=getDomOracleMultimapAsString();
		Helper.directoryCheck(outputFolder);
		String outputfolder=getOutputFolder();
		File file = new File(outputfolder + "domOracle" + ".txt");
		try {
		 Files.write( domOracleMap, file, Charsets.UTF_8 );
		} catch( IOException e ) {
		 
			e.printStackTrace();
		}
		
	}
	
	private String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}
	
	public ArrayListMultimap<String, ArrayListMultimap<String, DomAttribute>> getDomOracleMultimapFromFile(){
		ArrayListMultimap<String, ArrayListMultimap<String, DomAttribute>> domOracle=ArrayListMultimap.create();
		try{
			
			String filenameAndPath=getOutputFolder()+ this.fileName ;
			
			BufferedReader input =
					new BufferedReader(new FileReader(filenameAndPath));
					
			String line="";
			while ((line = input.readLine()) != null){
				
				String clickedOn_state=line.replace("clickedOn_state::","");
				String elementXpath="";
				ArrayListMultimap<String, DomAttribute> elemXpathAttr=ArrayListMultimap.create();
				
				while (!(line = input.readLine()).equals("===========================================================================")){
					
					if(line.contains("elementXpath")){
						elementXpath=line.split("::")[1];
						
					}
					else if(line.contains("attribute")){
						String attrName=line.split("::")[1];
						String attrValue=line.split("::")[2];
						DomAttribute domAttr=new DomAttribute(attrName, attrValue);
						elemXpathAttr.put(elementXpath, domAttr);
						
					}
				}
				
				domOracle.put(clickedOn_state,elemXpathAttr);
				
			}
			input.close();
			
		}
		
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return domOracle;
	}
	
	
	
}
