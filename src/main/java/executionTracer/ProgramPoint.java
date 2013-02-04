
package executionTracer;

import java.util.ArrayList;
import java.util.TreeSet;
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

	
	
	public String getData(String postfix, JSONArray data) throws CrawljaxException, JSONException {
		StringBuffer result = new StringBuffer();
		boolean found = false;

		result.append(name + postfix + "\n");
		for (Variable var : variables) {
			found=false;
			for (int i = 0; i < data.length(); i++) {
				JSONArray item = data.getJSONArray(i);

				if (var.getName().equals(item.getString(0))) {
							
					result.append(var.getData(item.get(2)));
					found = true;
					break;
				}
			}
			if (!found) {
				result.append(var.getData("undefined"));
				found = true;
			}
		}

		result.append("\n");

		return result.toString();
	}
	
	public void variable(Variable variable){
		variables.add(variable);
		
	}
}
