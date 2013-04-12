package executionTracer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

public class DOM_JS_Trace {
	private ArrayList<ProgramPoint> programPoints;


	
	public DOM_JS_Trace() {
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
				if(o.get(0).toString().equals("DOM")){
					
					String node=getDOMRelatedData(o.getJSONArray(1));
					String line=o.get(2).toString(); 
					String val= o.get(3).toString();
					String time= o.get(4).toString();
					DOMInput domInput=new DOMInput(node, line, val, time);
					prog.domInput(domInput);

				}
				else{
					prog.variable(Variable.parse(o));
				}
			}	
			
			result.append(prog.getData(value.getJSONArray(2)));
			
			
/*			if(j<jsonObject.length()-1){
				String isThisDom=value.getJSONArray(2).getJSONArray(0).getString(0);
				String isNextDom=jsonObject.getJSONArray(j+1).getJSONArray(2).getJSONArray(0).getString(0);
				String nextProgPointName=jsonObject.getJSONArray(j+1).getString(0)+jsonObject.getJSONArray(j+1).getString(1);
				if(isThisDom.equals("DOM") || isNextDom.equals("DOM")){
					if(programPointName.equals(nextProgPointName) ){
						shouldNextProgPointNameShown=false;
						continue;
					}
				}
				else {
					shouldNextProgPointNameShown=true;
					result.append("===========================================================================\n");
				
				}
			}
			
*/			result.append("===========================================================================\n");
				
			
			
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

	public String getDOMRelatedData(JSONArray data) throws CrawljaxException, JSONException {
		StringBuffer result = new StringBuffer();
		

	
		for (int i=0;i<data.length();i++) {
	//		result.append("node::");
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
//				result.append("\n");			
			}
		}
		
	

		return result.toString();
	}
	
}
