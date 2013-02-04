
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


		for (ProgramPoint p : programPoints) {
			if (p.getName().equals(name)) {
				return p;
			}
		}

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
	 */
	public static Trace parse(JSONArray jsonObject) throws JSONException {
		Trace trace = new Trace();
		for (int j = 0; j < jsonObject.length(); j++) {
			JSONArray value = jsonObject.getJSONArray(j);

			String prefix = value.getString(1);
			String programPointName = value.getString(0)+ prefix;
			
			ProgramPoint prog = trace.programPoint(programPointName);

			

			value = value.getJSONArray(2);
			/* output all the variable values */
			for (int i = 0; i < value.length(); i++) {
				JSONArray o = value.getJSONArray(i);
				prog.variable(Variable.parse(o));

			}
		}

		return trace;
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
			
			result.append(prog.getData(prefix, value.getJSONArray(2)));

		}

		return result.toString();
	}
}
