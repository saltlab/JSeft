package jsmodify;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ast.AstRoot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;

import com.crawljax.util.Helper;
import example.LocalExample;
import instrument.AstInstrumenter;
import com.google.common.io.Resources;

/**
 * The JSInstrument proxy plugin used to add instrumentation code to JavaScript files.
 */
public class JSModifyProxyPlugin extends ProxyPlugin {

	private List<String> excludeFilenamePatterns;

	public static List<String> visitedBaseUrls; 
	public static String scopeNameForExternalUse;

	private final JSASTModifier modifier;

	private static String outputFolder = "";
	private static String jsFilename = "";
	private static String targetFile = "";
	private static int targetLine = -1;
	private static String targetVariable = "";
	private static LocalExample le = new LocalExample();
	private static boolean firstTime = true;

	/**
	 * Construct without patterns.
	 * 
	 * @param modify
	 *            The JSASTModifier to run over all JavaScript.
	 */
	public JSModifyProxyPlugin(JSASTModifier modify) {
		excludeFilenamePatterns = new ArrayList<String>();
		visitedBaseUrls = new ArrayList<String>();

		modifier = modify;
		outputFolder = Helper.addFolderSlashIfNeeded("clematis-output") + "js_snapshot";
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param modify
	 *            The JSASTModifier to run over all JavaScript.
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public JSModifyProxyPlugin(JSASTModifier modify, List<String> excludes) {
		excludeFilenamePatterns = excludes;
		modifier = modify;
	}

	public void excludeDefaults() {
		excludeFilenamePatterns.add(".*jquery[-0-9.]*.js?.*");
		excludeFilenamePatterns.add(".*jquery.*.js?.*");
		excludeFilenamePatterns.add(".*prototype.*js?.*");
		excludeFilenamePatterns.add(".*scriptaculous.*.js?.*");
		excludeFilenamePatterns.add(".*mootools.js?.*");
		excludeFilenamePatterns.add(".*dojo.xd.js?.*");
		excludeFilenamePatterns.add(".*trial_toolbar.js?.*");

		// Example application specific
		excludeFilenamePatterns.add(".*tabcontent.js?.*");
		excludeFilenamePatterns.add(".*toolbar.js?.*");
		excludeFilenamePatterns.add(".*jquery*.js?.*");
		excludeFilenamePatterns.add(".*ga*.js?.*");

		// Don't instrument obfuscated code?
		excludeFilenamePatterns.add(".*min*.js?.*");
		excludeFilenamePatterns.add(".*bootstrap-tooltip*.js?.*");
		excludeFilenamePatterns.add(".*bootstrap-popover*.js?.*");
		excludeFilenamePatterns.add(".*handlebars*.js?.*");
		excludeFilenamePatterns.add(".*caramel.handlebars.client*.js?.*");
		excludeFilenamePatterns.add(".*caramel-client*.js?.*");
		//excludeFilenamePatterns.add(".*assets*.js?.*");
        excludeFilenamePatterns.add(".*asset-core*.js?.*");
        excludeFilenamePatterns.add(".*sort-assets*.js?.*");
	}

	public void setTargetFile (String t) {
		targetFile = t;
	}
	public void setLineNo (int l) {
		targetLine = l;
	}
	public void setVariableName (String t) {
		targetVariable = t;
	}

	@Override
	public String getPluginName() {
		return "JSInstrumentPlugin";
	}

	@Override
	public HTTPClient getProxyPlugin(HTTPClient in) {
		return new Plugin(in);
	}

	private boolean shouldModify(String name) {
		
		/* try all patterns and if 1 matches, return false */
		for (String pattern : excludeFilenamePatterns) {
			if (name.matches(pattern)) {
				return false;
			}
		}
		if (name.indexOf(targetFile) != -1 && (name.indexOf(targetFile) == (name.length() - targetFile.length()))) {
			firstTime = false;
			return true;
		} else if (name.indexOf(targetFile) != -1 && firstTime == true) {

		} else if (firstTime == false) {
			return true;
		}


		/*if (name.equals("http://www.themaninblue.com:80/experiment/BunnyHunt/scripts/bunnies.js")) {
			return true;
		}*/
		return false;
	}

	/**
	 * This method tries to add instrumentation code to the input it receives. The original input is
	 * returned if we can't parse the input correctly (which might have to do with the fact that the
	 * input is no JavaScript because the server uses a wrong Content-Type header for JSON data)
	 * 
	 * @param input
	 *            The JavaScript to be modified
	 * @param scopename
	 *            Name of the current scope (filename mostly)
	 * @return The modified JavaScript
	 */
	private synchronized String modifyJS(String input, String scopename) {
		

		scopeNameForExternalUse = scopename;

		if (!shouldModify(scopename)) {
			return input;
		} 
		
		try {

			le.setTargetFile(targetFile);
			le.setLineNo(targetLine);
			le.setVariableName(targetVariable);

			// Save original JavaScript files/nodes
			Helper.directoryCheck(getOutputFolder());
			Helper.directoryCheck(getOutputFolder()+"source/");
			Helper.checkFolderForFile("src/main/webapp/source/" + getFilename());

			setFileName(scopename);
			PrintStream oldOut = System.out;
			PrintStream outputVisual =
					new PrintStream("src/main/webapp/source/" + getFilename());

			// Save the original JavaScript file for displaying later
			
			String HTMLReadyCode = input.replaceAll("<", "&lt;");
			System.setOut(outputVisual);
			System.out.println(HTMLReadyCode);
			System.setOut(oldOut);

			String ast = le.instrument(input, "/"+getFilename());
			/* clean up */
			return ast;//.toSource();
		} catch (RhinoException re) {
			System.err.println(re.getMessage()
					+ "Unable to instrument. This might be a JSON response sent"
					+ " with the wrong Content-Type or a syntax error.");

			System.err.println("details: " + re.details());
			System.err.println("getLocalizedMessage: " + re.getLocalizedMessage());
			System.err.println("getScriptStackTrace: " + re.getScriptStackTrace());
			System.err.println("lineNumber: " + re.lineNumber());
			System.err.println("lineSource: " + re.lineSource());
			System.err.println("getCause: " + re.getCause());
			re.printStackTrace();

		} catch (IllegalArgumentException iae) {
			System.err.println("Invalid operator exception catched. Not instrumenting code.");

			System.err.println("getCause: " + iae.getCause());
			System.err.println("getLocalizedMessage: " + iae.getLocalizedMessage());
			System.err.println("getMessage: " + iae.getMessage());
			iae.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("Error saving original javascript files.");
			System.err.println("getMessage: " + ioe.getMessage());
			ioe.printStackTrace();
		} catch (Exception ee) {
			System.err.println("Error instrumenting code (check 'isArgument' function).");
			ee.printStackTrace();
		}
		System.err.println("Here is the corresponding buffer: \n" + input + "\n");

		return input;
	}

	private void setFileName(String scopename) {
		int index = scopename.lastIndexOf("/");
		jsFilename = scopename.substring(index + 1);
	}

	public static String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}

	private static String getFilename() {
		return jsFilename;
	}

	/**
	 * This method modifies the response to a request.
	 * 
	 * @param response
	 *            The response.
	 * @param request
	 *            The request.
	 * @return The modified response.
	 */
	private Response createResponse(Response response, Request request) {
		ArrayList<String> scriptNodesToCreate;
		Element newNodeToAdd;

		if (request == null) {
			//System.err.println("JSModifyProxyPlugin::createResponse: request is null");
			return response;
		}

		if (request.getURL() == null) {
			//System.err.println("JSModifyProxyPlugin::createResponse: request url is null");
			return response;
		} else if (request.getURL().toString().isEmpty()) {
			//System.err.println("JSModifyProxyPlugin::createResponse: request url is empty");
			return response;
		} else if (response == null) {
			//System.err.println("JSModifyProxyPlugin::createResponse: response is null");
			return response;
			// Proxy can provide Clematis files to prepend to application (specified in SimpleExample.java)
		} else if (!request.getURL().toString().contains("-clematis")
				&& Integer.parseInt(response.getStatus()) == 404
				&& modifier.getFilesToPrepend().contains(request.getURL().toString().substring(request.getURL().toString().lastIndexOf("/")))) {		
			return packageMessage(request, request.getURL().toString().substring(request.getURL().toString().lastIndexOf("/")));	
			// Proxy can provide JavaScript and CSS specific to toolbar
		} else if (request.getURL().toString().contains("toolbar-clematis") && Integer.parseInt(response.getStatus()) == 404) {		
			return packageMessage(request,request.getURL().toString().substring(request.getURL().toString().lastIndexOf("/toolbar-clematis/")));
			// Proxy can provide images for toolbar rendering
		} else if (request.getURL().toString().contains("/images-clematis/") && Integer.parseInt(response.getStatus()) == 404) {
			return packageMessage(request, request.getURL().toString().substring(request.getURL().toString().lastIndexOf("/images-clematis/")));
		}

		String type = response.getHeader("Content-Type");

		// Communication with client in regards to recording
		if (request.getURL().toString().contains("?slicinginformation")) {
			String rawResponse = new String(request.getContent());
			JSExecutionTracer.parseReadWrites(rawResponse);
			return response;
		}

		// Intercept and instrument relevant files (JavaScript and HTML)
		if (type != null && type.contains("javascript")) {
			/* instrument the code if possible */
			response.setContent(modifyJS(new String(response.getContent()),
					request.getURL().toString()).getBytes());
		} else if (type != null && type.contains("html")) {

			try {
				Document dom = Helper.getDocument(new String(response.getContent()));
				/* find script nodes in the html */
				NodeList nodes = dom.getElementsByTagName("script");

				for (int i = 0; i < nodes.getLength(); i++) {
					Node nType = nodes.item(i).getAttributes().getNamedItem("type");
					/* instrument if this is a JavaScript node */
					if ((nType != null && nType.getTextContent() != null && nType
							.getTextContent().toLowerCase().contains("javascript"))) {
						String content = nodes.item(i).getTextContent();

						if (content.length() > 0) {
							String js = modifyJS(content, request.getURL() + "script" + i);
							nodes.item(i).setTextContent(js);
							continue;
						}
					}
					/* also check for the less used language="javascript" type tag */
					nType = nodes.item(i).getAttributes().getNamedItem("language");
					if ((nType != null && nType.getTextContent() != null && nType
							.getTextContent().toLowerCase().contains("javascript"))) {
						String content = nodes.item(i).getTextContent();
						if (content.length() > 0) {
							String js = modifyJS(content, request.getURL() + "script" + i);
							nodes.item(i).setTextContent(js);
						}
					}
				}

				// Add our JavaScript as script nodes instead of appending the file contents to existing JavaScript
				scriptNodesToCreate = modifier.getFilesToPrepend();
				for (int p = 0; p < scriptNodesToCreate.size(); p++) {
					newNodeToAdd = dom.createElement("script");					
					newNodeToAdd.setAttribute("src", scriptNodesToCreate.get(p));
					newNodeToAdd.setAttribute("language", "javascript");
					newNodeToAdd.setAttribute("type", "text/javascript");					
					if (dom.getElementsByTagName("meta").getLength() != 0 
							&& dom.getElementsByTagName("meta").item(0).getParentNode() == dom.getElementsByTagName("head").item(0)) {
						dom.getElementsByTagName("head").item(0).insertBefore(newNodeToAdd, dom.getElementsByTagName("meta").item(dom.getElementsByTagName("meta").getLength()-1));
					}
				}

				// Inject toolbar and its dependencies
				scriptNodesToCreate = modifier.getToolbarFiles();
				for (int t = 0; t < scriptNodesToCreate.size(); t++) {

					if (scriptNodesToCreate.get(t).contains(".js")) {
						// JavaScript
						newNodeToAdd = dom.createElement("script");	
						newNodeToAdd.setAttribute("language", "javascript");
						newNodeToAdd.setAttribute("type", "text/javascript");	
						newNodeToAdd.setAttribute("src", scriptNodesToCreate.get(t));
					} else if (scriptNodesToCreate.get(t).contains(".css")) {
						// CSS
						newNodeToAdd = dom.createElement("link");			
						newNodeToAdd.setAttribute("rel", "stylesheet");
						newNodeToAdd.setAttribute("type", "text/css");
						newNodeToAdd.setAttribute("href", scriptNodesToCreate.get(t));
					} else {
						// File type not supported
						continue;
					}
					// Insert our scripts in the <head> right after the <meta> tags (before all applications scripts)
					if (dom.getElementsByTagName("meta").getLength() != 0 && dom.getElementsByTagName("meta").item(0).getParentNode() == dom.getElementsByTagName("head").item(0)) {
						dom.getElementsByTagName("head").item(0).insertBefore(newNodeToAdd, dom.getElementsByTagName("meta").item(dom.getElementsByTagName("meta").getLength()-1));
					}
				}

				/* only modify content when we did modify anything */
				if (nodes.getLength() > 0) {
					/* set the new content */
					response.setContent(Helper.getDocumentToByteArray(dom));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/* return the response to the webbrowser */
		return response;
	}

	private Response packageMessage(Request request, String file) {
		Response intrResponse = new Response();
		intrResponse.setStatus("200");
		intrResponse.setVersion("HTTP/1.1");
		intrResponse.setRequest(request);
		intrResponse.setMessage("OK");
		intrResponse.setHeader("Connection", "close");

		try {
			intrResponse.setContent(Resources.toByteArray(AstInstrumenter.class.getResource(file)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//System.out.println(file);
			e.printStackTrace();
		} catch (NullPointerException npe) {
			//System.out.println(file);
			npe.printStackTrace();
		}
		return intrResponse;
	}

	/**
	 * WebScarab plugin that adds instrumentation code.
	 */
	private class Plugin implements HTTPClient {

		private HTTPClient client = null;

		/**
		 * Constructor for this plugin.
		 * 
		 * @param in
		 *            The HTTPClient connection.
		 */
		public Plugin(HTTPClient in) {
			client = in;
		}

		public Response fetchResponse(Request request) throws IOException {

			Response response = client.fetchResponse(request);
			return createResponse(response, request);
		}

	}

}
