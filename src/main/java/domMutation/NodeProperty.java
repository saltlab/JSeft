package domMutation;

import java.util.ArrayList;
import java.util.Arrays;

public class NodeProperty {
	private Node node;
	private String line;
	private String value;
	private String typeOfAccess;
	private String property="";
	
	public NodeProperty(Node node, String line, String value){
		ArrayList<String> accessType=new ArrayList<>( Arrays.asList(".css", ".attr", "DIRECTACCESS"));
		
		this.node=node;
		this.line=line;
		this.value=value;
		for(String type:accessType){
			if(line.contains(type)){
				typeOfAccess=type;
				String[] str=line.split(type);
				property=str[str.length-1].replace("(", "").replace(")","");
				break;
			}
			else{
				if(value.equals("DIRECTACCESS")){
					typeOfAccess="DIRECTACCESS";
					property="DIRECTACCESS";
					break;
				}
			}
		}
	}
	
	public Node getNode(){
		return node;
	}
	
	public String getLine(){
		return line;
	}
	
	public String getValue(){
		return value;
	}
	public String getTypeOfAccess(){
		return typeOfAccess;
	}
	public String getProperty(){
		return property;
	}
	
}
