package domMutation;

public class NodeProperty {
	private Node node;
	private String line;
	private String value;
	
	public NodeProperty(Node node, String line, String value){
		this.node=node;
		this.line=line;
		this.value=value;
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
	
}
