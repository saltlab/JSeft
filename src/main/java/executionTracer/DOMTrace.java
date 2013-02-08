package executionTracer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

public class DOMTrace {
private ArrayList<ProgramPoint> programPoints;


	
	public DOMTrace() {
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
			String domProgramPointName = value.getString(0)+ prefix;
			result.append(domProgramPointName + "\n");
			DOMProgramPoint prog = new DOMProgramPoint(domProgramPointName);
		
			JSONArray jasonvalue = value.getJSONArray(2);
			for (int i = 0; i < jasonvalue.length(); i++) {
				JSONArray o = jasonvalue.getJSONArray(i);
				result.append("node::" + prog.getData(o.getJSONArray(0)));
				result.append("property::" + o.get(1) +"\n"); 
				result.append("value::" + o.get(2));
				result.append("\n");

			}	
				
			result.append("===========================================================================\n");
				
			
			
		}

		return result.toString();
	}



}
