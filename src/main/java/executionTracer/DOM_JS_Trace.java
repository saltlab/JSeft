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
	
		String result="";
	
		for (int j = 0; j < jsonObject.length(); j++) {
			
			JSONArray value = jsonObject.getJSONArray(j);

			String prefix = value.getString(1);
			String programPointName = value.getString(0)+ prefix;
			ProgramPoint prog = new ProgramPoint(programPointName);
			if(prefix.equals(ProgramPoint.EXITPOSTFIX)){
				result+=getTraceRecordForExitPoint(value, prog);

				
			}
			else{
				result+=getTraceRecordforEntryPoint(value, prog);
			}
				
					
			
		}

		return result;
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

	@Deprecated
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
	
	
	private String getTraceRecordForExitPoint(JSONArray value, ProgramPoint prog) throws JSONException{
		StringBuffer result = new StringBuffer();
		int indexForAddVariablePart;
	
		JSONArray domjasonvalue = value.getJSONArray(2);
		if(domjasonvalue.get(0).toString().equals("DOM")){
			for (int i = 1; i < domjasonvalue.length(); i++) {
				JSONArray o = domjasonvalue.getJSONArray(i);
				if(o.get(0) instanceof JSONArray){
					JSONArray nodeArray=o.getJSONArray(0);
					for(int j=0;j<nodeArray.length();j++){
				
						String node=nodeArray.get(j).toString();
				//		String line=o.get(1).toString(); 
				//		String val=o.get(2).toString();
						String line="";
						String val="";
						DOMOutPut domOutput=new DOMOutPut(node, line, val);
						prog.domOutPut(domOutput);
					}
				}
			}
			indexForAddVariablePart=3;
		}
		
		else{
			indexForAddVariablePart=2;
		}
		
		JSONArray varjasonvalue = value.getJSONArray(indexForAddVariablePart);
		for (int i = 0; i < varjasonvalue.length(); i++) {
			JSONArray o = varjasonvalue.getJSONArray(i);			
			prog.variable(Variable.parse(o));
			
			
		}	
		
		result.append(prog.getData(value.getJSONArray(indexForAddVariablePart)));
		result.append(prog.getDomOutPutData());
		result.append("===========================================================================\n");
			

		return result.toString();

	}
	
	private String getTraceRecordforEntryPoint(JSONArray value, ProgramPoint prog) throws JSONException{
		StringBuffer result = new StringBuffer();
		String domHtml=value.getString(2);
		DOMInput domInput=new DOMInput(domHtml);
		prog.domInput(domInput);
		JSONArray jasonvalue = value.getJSONArray(3);

		for (int i = 0; i < jasonvalue.length(); i++) {
			JSONArray o = jasonvalue.getJSONArray(i);
/*			if(o.get(0).toString().equals("DOM")){
				
				String node=getDOMRelatedData(o.getJSONArray(1));
				String line=o.get(2).toString(); 
				String val= o.get(3).toString();
				String time= o.get(4).toString();
				DOMInput domInput=new DOMInput(node, line, val, time);
				prog.domInput(domInput);

			}
*/			
			prog.variable(Variable.parse(o));
			
			
		}	
		
		
		result.append(prog.getData(value.getJSONArray(3)));			
		result.append("===========================================================================\n");

		return result.toString();
	}
	
}
