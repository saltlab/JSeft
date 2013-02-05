
package executionTracer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;



public class Trace {
	private ArrayList<ProgramPoint> programPoints;


	
	public Trace() {
		programPoints = new ArrayList<ProgramPoint>();

		
	}

	/**
	 * Get or create a program point defined by name.
	 * 
	 * @param name
	 *            Name of the program point.
	 * @return The ProgramPoint object.
	 */
	public ProgramPoint programPoint(String name) {


	

		ProgramPoint p = new ProgramPoint(name);
		programPoints.add(p);

		return p;
	}

	/**
	 * Parse JSON object into a trace.
	 * 
	 * @param jsonObject
	 *            The JSON object.
	 * @return The trace object.
	 * @throws JSONException
	 *             On error.
	 * @throws CrawljaxException 
	 */
	public String getTraceRecord(JSONArray jsonObject) throws JSONException, CrawljaxException {
	
		StringBuffer result = new StringBuffer();
		for (int j = 0; j < jsonObject.length(); j++) {
			JSONArray value = jsonObject.getJSONArray(j);

			String prefix = value.getString(1);
			String programPointName = value.getString(0)+ prefix;
			
			ProgramPoint prog = new ProgramPoint(programPointName);
		
			JSONArray jasonvalue = value.getJSONArray(2);
			for (int i = 0; i < jasonvalue.length(); i++) {
				JSONArray o = jasonvalue.getJSONArray(i);
				prog.variable(Variable.parse(o));

			}	
			result.append(prog.getData(value.getJSONArray(2)));
				
			result.append("===========================================================================\n");
				
			
			
		}

		return result.toString();
	}



	/**
	 * Returns all data trace records.
	 * 
	 * @param jsonObject
	 *            Raw trace object.
	 * @return  data trace records as a String.
	 * @throws CrawljaxException
	 *             When an unsupported type is encountered.
	 * @throws JSONException
	 *             On error.
	 */
	public String getData(JSONArray jsonObject) throws CrawljaxException, JSONException {
		StringBuffer result = new StringBuffer();

	
		for (int j = 0; j < jsonObject.length(); j++) {
			JSONArray value = jsonObject.getJSONArray(j);
			String prefix = value.getString(1);
			String programPointName = value.getString(0)+prefix;
			
			ProgramPoint prog = programPoint(programPointName);
			
			
			result.append(prog.getData(value.getJSONArray(2)));
			if(j<jsonObject.length()-1){
				if(!jsonObject.getJSONArray(j+1).getString(0).equals(value.getString(0)) ||
						prefix.equals(ProgramPoint.EXITPOSTFIX) &&jsonObject.getJSONArray(j+1).getString(1).equals(ProgramPoint.ENTERPOSTFIX)){
					result.append("===========================================================================\n");
				}
			}
	
	
		}

		return result.toString();
	}
}
