
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



	/**
	 * Construct a new program point representation.
	 * 
	 * @param name
	 *            The name of the program point.
	 */
	public ProgramPoint(String name) {
		this.name = name;
		variables = new ArrayList<Variable>();
	

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
					break;
				}
			}
			if (!found) {
				result.append(var.getData("Undefined"));
				result.append("variableUsage::" + variableUsage + "\n");
				found = true;
			}
		}

		result.append("time::" + time + "\n");

		return result.toString();
	}
	
	public void variable(Variable variable){
		variables.add(variable);
		
	}
}
