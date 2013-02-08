package domMutation;

public class NodeProperty {
	private String node;
	private String line;
	private String value;
	
	public NodeProperty(String node, String line, String value){
		this.node=node;
		this.line=line;
		this.value=value;
	}
	
	public String getNode(){
		return node;
	}
	
	public String getLine(){
		return line;
	}
	
	public String getValue(){
		return value;
	}
	
/*	public String extractFuncNameFromLine(){
		String[] str=line.split("[.:::]");
		return str[str.length-2];
	}
*/
}
