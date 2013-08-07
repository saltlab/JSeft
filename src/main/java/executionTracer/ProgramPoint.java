
package executionTracer;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;


public class ProgramPoint {

	public static final String ENTERPOSTFIX = ":::ENTER";
	public static final String EXITPOSTFIX = ":::EXIT";
	public static final String POINTPOSTFIX = ":::POINT";

	private String name;
	private ArrayList<Variable> variables;
	private ArrayList<DOMInput> domInputs;
	private ArrayList<DOMOutPut> domOutPuts;
	private String coverage;


	/**
	 * Construct a new program point representation.
	 * 
	 * @param name
	 *            The name of the program point.
	 */
	public ProgramPoint(String name, String coverage) {
		this.name = name;
		variables = new ArrayList<Variable>();
		domInputs=new ArrayList<DOMInput>();
		domOutPuts=new ArrayList<DOMOutPut>();
		this.coverage=coverage;
	

	}
	
	private String getDomInputData(){
		StringBuffer result = new StringBuffer();
		for(DOMInput domInput:domInputs){
			result.append("dom::" + domInput.getDomHtml() + "\n");
		}
		return result.toString();
	}


	public String getDomOutPutData(){
		StringBuffer result = new StringBuffer();
		for(DOMOutPut domOutput:domOutPuts){
			result.append("node::" + domOutput.getNode() + "\n");
/*			result.append("line::" + domOutput.getLine() + "\n");
			result.append("value::" + domOutput.getValue() + "\n");
*/		}
		return result.toString();
	}
	
	public String getCoverage(){
		StringBuffer result = new StringBuffer();
		result.append("coverage::" + coverage + "\n");
		return result.toString();
	}


	/**
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	
	
	public String getData(JSONArray data) throws CrawljaxException, JSONException {
		StringBuffer result = new StringBuffer();
		String time = "";
		String variableUsage="";
		boolean found = false;

		
		result.append(name + "\n");

		if(name.contains(ProgramPoint.ENTERPOSTFIX))
			result.append(getDomInputData());

		for (Variable var : variables) {
			found=false;
			for (int i = 0; i < data.length(); i++) {
				JSONArray item = data.getJSONArray(i);
				
				time=item.get(3).toString();
				variableUsage=item.get(4).toString();
				if (var.getName().equals(item.getString(0))) {
							
					result.append(var.getData(item.get(2)));
					result.append("variableUsage::" + variableUsage + "\n");
					found = true;
	//				break;
				}
			}
			if (!found) {
				result.append(var.getData("Undefined"));
//				result.append("variableUsage::" + variableUsage + "\n");
				found = true;
			}
		}

/*		if(data.getJSONArray(0).get(0).toString().equals("DOM")){
			result.append(getDomInputData());
		}
*/		
		result.append("time::" + time + "\n");
		return result.toString();
	}
	
	public void variable(Variable variable){
		variables.add(variable);
		
	}
	public void domInput(DOMInput domInput){
		domInputs.add(domInput);
	}
	
	public void domOutPut(DOMOutPut domOutput){
		domOutPuts.add(domOutput);
	}
}
