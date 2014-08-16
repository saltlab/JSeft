package jsmodify;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.crawljax.util.Helper;

/**
 * Reads an instrumentation array from the webbrowser and saves the contents in a JSON trace file.
 * 
 * @author Frank Groeneveld
 * @version $Id: JSExecutionTracer.java 6162 2009-12-16 13:56:21Z frank $
 */
public class JSExecutionTracer {

	private static String outputFolder;
	private static String traceFilename;

	private static JSONArray points = new JSONArray();

	private static final Logger LOGGER = Logger
			.getLogger(JSExecutionTracer.class.getName());

	public static final String READWRITEDIRECTORY = "readswrites/";

	private static PrintStream output;

	private static int counter = 0;

	public JSExecutionTracer() {
	}

	/**
	 * Initialize the plugin and create folders if needed.
	 * 
	 * @param browser
	 *            The browser.
	 */
	public static void preCrawling() {
		try {
			Helper.directoryCheck(getOutputFolder());
			output = new PrintStream(getOutputFolder() + getFilename());

			// Add opening bracket around whole trace
			PrintStream oldOut = System.out;
			System.setOut(output);
			System.out.println("{");
			System.setOut(oldOut);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void postCrawling() {
		try {
			// Add closing bracket
			PrintStream oldOut = System.out;
			System.setOut(output);
			System.out.println(" ");
			System.out.println("}");
			System.setOut(oldOut);

			/* close the output file */
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void parseReadWrites (String bunchOfRWs) {
		JSONArray buffer = null;
		JSONObject targetAttributes = null;
		JSONObject targetElement = null;
		String JSONLabel = new String();
		int i;

		try {
			/* save the current System.out for later usage */
			PrintStream oldOut = System.out;
			/* redirect it to the file */

			System.setOut(output);


			buffer = new JSONArray(bunchOfRWs);
			for (i = 0; i < buffer.length(); i++) {

				if (points.length() > 0) {
					// Add comma after previous trace object
					System.out.println(",");
				}

				points.put(buffer.getJSONObject(i));

				// Insert @class key for Jackson mapping
				if (buffer.getJSONObject(i).has("messageType")) {
					String mType = buffer.getJSONObject(i).get("messageType")
							.toString();

					// Maybe better to change mType to ENUM and use switch
					// instead of 'if's



					if (mType.contains("VARIABLE_WRITE_ADDSUB")) {
						buffer.getJSONObject(i).put("@class",
								"com.camellia.core.trace.VariableWriteAugmentAssign");
						JSONLabel = "\"VariableWriteAugmentAssign\":";
					} else if (mType.contains("READ_AS_ARGUMENT")) {
						buffer.getJSONObject(i).put("@class",
								"com.camellia.core.trace.ArgumentRead");
						JSONLabel = "\"ArgumentRead\":";
					} else if (mType.contains("WRITE_AS_ARGUMENT")) {
						buffer.getJSONObject(i).put("@class",
								"com.camellia.core.trace.ArgumentWrite");
						JSONLabel = "\"ArgumentWrite\":";
					} else if (mType.contains("VARIABLE_WRITE")) {
						buffer.getJSONObject(i).put("@class",
								"com.camellia.core.trace.VariableWrite");
						JSONLabel = "\"VariableWrite\":";
					} else if (mType.contains("VARIABLE_READ")) {
						buffer.getJSONObject(i).put("@class",
								"com.camellia.core.trace.VariableRead");
						JSONLabel = "\"VariableRead\":";
					} else if (mType.contains("WRITE_RETURN_VALUE")) {
						buffer.getJSONObject(i).put("@class",
								"com.camellia.core.trace.ReturnValueWrite");
						JSONLabel = "\"ReturnValueWrite\":";
					} else if (mType.contains("PROPERTY_READ")) {
						buffer.getJSONObject(i).put("@class",
								"com.camellia.core.trace.PropertyRead");
						JSONLabel = "\"PropertyRead\":";
					} else if (mType.contains("WRITE_RETURN_VALUE")) {
						buffer.getJSONObject(i).put("@class",
								"com.camellia.core.trace.ReturnValueWrite");
						JSONLabel = "\"ReturnValueWrite\":";
					} else if (mType.contains("RETURN_VALUE")) {
						buffer.getJSONObject(i).put("@class",
								"com.camellia.core.trace.ReturnStatementValue");
						JSONLabel = "\"ReturnStatementValue\":";
					} else {
						buffer.getJSONObject(i).put("@class",
								"com.camellia.core.trace.Unknown");
						JSONLabel = "\"UnknownInstruction\":";
					}
					// messageType obsolete
					buffer.getJSONObject(i).remove("messageType");
				}

				System.out.print(JSONLabel + "["
						+ buffer.getJSONObject(i).toString(2) + "]");
			}

			/* Restore the old system.out */
			System.setOut(oldOut);

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return Name of the file.
	 */
	public static String getFilename() {
		return traceFilename;
	}

	public static String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}

	public void setOutputFolder(String absolutePath) {
		outputFolder = absolutePath;
	}

}
