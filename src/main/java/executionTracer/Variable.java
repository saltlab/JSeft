
package executionTracer;

import java.text.NumberFormat;
import java.util.Formatter;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;
import com.crawljax.util.Helper;


public class Variable {
	private String name;
	private String type;
	private boolean array;
	private Variable enclosingVariable;


	public Variable(String name, String type, boolean array, Variable enclosingVariable) {
		this.name = name;
		this.type = type;
		this.array = array;
		this.enclosingVariable = enclosingVariable;
	}


	private String getValue(Object value) throws CrawljaxException, JSONException {
		if (value instanceof JSONArray) {
			return getArray((JSONArray) value,type);
		} 
		else 
			if (isArray() && !(value instanceof JSONArray)){
				Object[] valueArray=new Object[1];
				valueArray[0]=value;
				return getnonJSONarrays(valueArray, type);
			}

		else {
			return getValue(value, type);
		}
	}


	private String getValue(Object value, String type) throws CrawljaxException, JSONException {
		if (value == null) {
			return "null";
		}
/*		if (value.toString().equals("null")){
			return null;
		}
*/		if (type.equals("string")) {

			
			/** support for two dim string array*/
			String result="[";
			if (value instanceof JSONArray) {

			    JSONArray json=(JSONArray) value;
					
				if (json.length()>0 ) {
					/* make sure it fits on 1 line by removing new line chars */
					value = Helper.removeNewLines(json.get(0).toString());
					/* escape quotes */
					value = ((String) value).replaceAll("\\\"", "\\\\\"");
					
					result+= "\"" + value.toString() + "\"";
				}
					for (int i=1;i< json.length();i++) {
						
						/* make sure it fits on 1 line by removing new line chars */
						value = Helper.removeNewLines(json.get(i).toString());
						/* escape quotes */
						value = ((String) value).replaceAll("\\\"", "\\\\\"");

						result+= "," + "\"" + value.toString() + "\"";
					}
				
				result+="]";
				return result;
			}
			else{
				/* make sure it fits on 1 line by removing new line chars */
				value = Helper.removeNewLines(value.toString());
				/* escape quotes */
				value = ((String) value).replaceAll("\\\"", "\\\\\"");
				return "\"" + value.toString() + "\"";
			}
			
			
		

		} else if (type.equals("number")) {
		    
			return value.toString();

		} else if (type.equals("boolean")) {
			String result="[";
				if (value instanceof JSONArray) {
				    JSONArray json=(JSONArray) value;
						
					if (json.length()>0 )
						if (json.get(0).toString().equals("true")) {
							result+= "1";
						} else {
							result+= "0";						
						}
					for (int i=1;i< json.length();i++) 
					
						if (json.get(i).toString().equals("true")) {
							result+= ",1";
						} else {
							result+= ",0";
						}
					result+="]";
					return result;
				}
				else{
	//				if (type.equals("boolean"))
						if (value.toString()=="true")
							return("1");
						else
							return ("0");
				}
		} else {
			//if (type.equals("object")) {
			return "\"" + value.toString() + "\"";
		}

	//	throw new CrawljaxException("Unhandled type when converting to trace file " + type);
	}

	

	private String getArray(JSONArray array, String type) throws CrawljaxException, JSONException {
		String result = "[";
        
		for (int i = 0; i < array.length(); i++) {
			if (i != 0) {
				result += " ";
			}
			result += getValue(array.get(i), type);
		}
		result=result + "]";
		if (isArray()){ 
			if(result.endsWith("]]")){// && result.endsWith("]]")){
				result="["+result.replace("[","").replace("]", "").replace(",", " ")+"]";
//				result=result.replace(",", " ");
				}

		}
		return result;
	}
	
	
	private String getnonJSONarrays(Object[] array, String type) throws CrawljaxException, JSONException {
		String result = "[";
        
		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				result += " ";
			}
			result += getValue(array[i], type);
		}
		result=result + "]";
		if (isArray()){ 
			if(result.endsWith("]]")){// && result.endsWith("]]")){
				result="["+result.replace("[","").replace("]", "").replace(",", " ")+"]";
//				result=result.replace(",", " ");
				}

		}
		return result;
	}

	
	String getDeclaration() throws CrawljaxException {
		StringBuffer varDecl = new StringBuffer();

		if (isArray()) {
			varDecl.append("variable " + name + "[..]\n");
			
			
		} else {
			varDecl.append("variable " + name + "\n");
		}
		/**
		 * the following if is for tictactoe only
		 */
/*		if (isArray() && type.equals("undefined")){

			varDecl.append("\t\tdec-type " + "number" + "\n");
		}
		else 
*/		varDecl.append("type " + type + "\n");
		

		
		
	
		varDecl.append("\n");

		return varDecl.toString();
	}

	/**
	 * Parses a JSON object into a Variable object.
	 * 
	 * @param var
	 *            The JSON object.
	 * @return The variable object.
	 * @throws JSONException
	 *             On error.
	 */
	public static Variable parse(JSONArray var) throws JSONException {
		/* retrieve the three values from the array */
		String name = var.getString(0);
		String type = (String) var.getString(1);
		Object value;
		try {
			value = var.getJSONArray(2);
		} catch (JSONException e) {
			value = var.getString(2);
			/* make sure it fits on 1 line by removing new line chars */
			value = Helper.removeNewLines((String) value);
			/* escape quotes */
			value = ((String) value).replaceAll("\\\"", "\\\\\"");
		}

		if (type.endsWith("_array")) {

			type = type.replaceAll("_array", "");

			Variable enclosingVariable = new Variable(name, "pointer", false, null);

			return new Variable(name, type, true, enclosingVariable);
		} else {
			return new Variable(name, type, false, null);
		}
	}


	public String getData(Object value) throws CrawljaxException, JSONException {

		return this.toString() + "\n" + getValue(value) + "\n" + getDeclaration();
	}

	/**
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return The array.
	 */
	public boolean isArray() {
		return array;
	}

	/**
	 * @return The enclosingVariable.
	 */
	public Variable getEnclosingVariable() {
		return enclosingVariable;
	}

	@Override
	public String toString() {
		String localName = name;
		if (isArray()) {
			localName += "[..]";
		}
		return localName;
	}
}
