package executionTracer;



import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

public class DOMProgramPoint {

	public static final String ENTERPOSTFIX = ":::ENTER";
	public static final String EXITPOSTFIX = ":::EXIT";
	public static final String POINTPOSTFIX = ":::POINT";

	private String name;
	



	/**
	 * Construct a new program point representation.
	 * 
	 * @param name
	 *            The name of the program point.
	 */
	public DOMProgramPoint(String name) {
		this.name = name;
	
	

	}





	/**
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	
	
	public String getData(JSONArray data) throws CrawljaxException, JSONException {
		StringBuffer result = new StringBuffer();
		

	
		for (int i=0;i<data.length();i++) {
			result.append("node::");
			if(data.get(i) instanceof JSONArray){
				JSONArray array=(JSONArray) data.get(i);
				for(int j=0;j<array.length();j++){
					result.append(array.get(j));
					result.append(",");
				}
				String temp=result.substring(0, result.length() - 1);
				result=new StringBuffer(temp);
			}
			else{
				result.append(data.get(i));
				result.append("\n");			
			}
		}
		
	

		return result.toString();
	}
	
	
}
