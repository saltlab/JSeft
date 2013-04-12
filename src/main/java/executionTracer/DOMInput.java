package executionTracer;

public class DOMInput {

	private String node;
	private String line;
	private String value;
	private String time;
	
	public DOMInput(String node, String line, String value, String time){
		this.node=node;
		this.line=line;
		this.value=value;
		this.time=time;
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
	public String getTime(){
		return time;
	}
	
}
