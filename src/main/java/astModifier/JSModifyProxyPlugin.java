package astModifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ast.AstRoot;
import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.util.Helper;
import com.google.common.base.Charsets;

import executionTracer.DOMExecutionTracer;
import executionTracer.DOM_JS_ExecutionTracer;
import executionTracer.JSExecutionTracer;


public class JSModifyProxyPlugin extends ProxyPlugin {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JSModifyProxyPlugin.class.getName());
	private List<String> excludeFilenamePatterns;

	private JSASTModifier modifier;
	private DOMASTModifier domModifier;
	private DOMMuteASTModifier domMuteModifier;
	private DOM_JS_ASTModifier dom_js_modifier;
	private boolean jsModify=false;
	private boolean domModify=false;
	private boolean domMuteModify=false;
	private boolean dom_js_modify=false;
	private boolean noModification=false;
	private String outputfolder;


	/**
	 * Construct without patterns.
	 * 
	 * @param modify
	 *            The JSASTModifier to run over all JavaScript.
	 */
	public JSModifyProxyPlugin(){
		excludeFilenamePatterns = new ArrayList<String>();
		this.noModification=true;
	}
	public JSModifyProxyPlugin(JSASTModifier modify) {
		
		excludeFilenamePatterns = new ArrayList<String>();
		modifier = modify;
		jsModify=true;
		domModify=false;
		this.dom_js_modify=false;
		this.domMuteModify=false;
		
	}
	
	public JSModifyProxyPlugin(DOM_JS_ASTModifier dom_js_modify) {
		
		excludeFilenamePatterns = new ArrayList<String>();
		dom_js_modifier = dom_js_modify;
		jsModify=false;
		domModify=false;
		this.domMuteModify=false;
		this.dom_js_modify=true;
		
	}
	
	
	
	public JSModifyProxyPlugin(DOMASTModifier domModify) {
		
		excludeFilenamePatterns = new ArrayList<String>();
		domModifier = domModify;
		this.domModify=true;
		this.jsModify=false;
		this.domMuteModify=false;
		this.dom_js_modify=false;
		
	}
	
	public JSModifyProxyPlugin(DOMMuteASTModifier domMuteModify) {
		
		excludeFilenamePatterns = new ArrayList<String>();
		domMuteModifier = domMuteModify;
		this.domModify=false;
		this.jsModify=false;
		this.domMuteModify=true;
		this.dom_js_modify=false;
		
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
	
	public JSModifyProxyPlugin(String outputfolder){
		excludeFilenamePatterns = new ArrayList<String>();
		this.outputfolder=outputfolder;
	}


	

	/**
	 * Adds some defaults to the list of files that should be excluded from modification. These
	 * include:
	 * <ul>
	 * <li>jQuery</li>
	 * <li>Prototype</li>
	 * <li>Scriptaculous</li>
	 * <li>MooTools</li>
	 * <li>Dojo</li>
	 * <li>YUI</li>
	 * <li>All kinds of Google scripts (Adwords, Analytics, etc)</li>
	 * <li>Minified JavaScript files with min, compressed or pack in the URL.</li>
	 * </ul>
	 */
	public void excludeDefaults() {
		excludeFilenamePatterns.add(".*jquery[-0-9.]*.js?.*");
		excludeFilenamePatterns.add(".*jquery-getpath*.js?.*");
		excludeFilenamePatterns.add(".*seedrandom-min*.js?.*");
		excludeFilenamePatterns.add(".*jquery*.js?.*");
		excludeFilenamePatterns.add(".*jquery.*.js?.*");
	//	excludeFilenamePatterns.add(".*same-game.*.htm?.*");
		excludeFilenamePatterns.add(".*prototype.*js?.*");
		excludeFilenamePatterns.add(".*scriptaculous.*.js?.*");
		excludeFilenamePatterns.add(".*mootools.js?.*");
		excludeFilenamePatterns.add(".*dojo.xd.js?.*");
		excludeFilenamePatterns.add(".*dojo.js.uncompressed?.*");
		excludeFilenamePatterns.add(".*dojo.js?.*");
		excludeFilenamePatterns.add(".*yuiloader.js?.*");
		excludeFilenamePatterns.add(".*google.*");
		excludeFilenamePatterns.add(".*min.*.js?.*");
		excludeFilenamePatterns.add(".*pack.*.js?.*");
		excludeFilenamePatterns.add(".*compressed.*.js?.*");
		excludeFilenamePatterns.add(".*rpc.*.js?.*");
		excludeFilenamePatterns.add(".*o9dKSTNLPEg.*.js?.*");
		excludeFilenamePatterns.add(".*gdn6pnx.*.js?.*");
		excludeFilenamePatterns.add(".*show_ads.*.js?.*");
		excludeFilenamePatterns.add(".*ga.*.js?.*");
		excludeFilenamePatterns.add(".*cycle.*.js?.*");
		
		//The following 10 excluded files are just for Tudu
		excludeFilenamePatterns.add(".*builder.js");
		excludeFilenamePatterns.add(".*controls.js");
		excludeFilenamePatterns.add(".*dragdrop.js");
		excludeFilenamePatterns.add(".*effects.js");
		excludeFilenamePatterns.add(".*prototype.js");
		excludeFilenamePatterns.add(".*scriptaculous.js");
		excludeFilenamePatterns.add(".*slider.js");
		excludeFilenamePatterns.add(".*unittest.js");
	//	excludeFilenamePatterns.add(".*engine.js");
		excludeFilenamePatterns.add(".*util.js");
		///////
		excludeFilenamePatterns.add(".*qunit.js");
		excludeFilenamePatterns.add(".*filesystem.js");
		excludeFilenamePatterns.add(".*functional.js");
		excludeFilenamePatterns.add(".*test.core.js");
		excludeFilenamePatterns.add(".*inject.js");
		/* for fractal viewr */
		excludeFilenamePatterns.add(".*Branch.js?.*");
		excludeFilenamePatterns.add(".*Brush.js?.*");
		excludeFilenamePatterns.add(".*Leaf.js?.*");
		excludeFilenamePatterns.add(".*Rect2.js?.*");
		excludeFilenamePatterns.add(".*the_bones.js?.*");
//		excludeFilenamePatterns.add(".*the_meat.js?.*");
		excludeFilenamePatterns.add(".*the_ui.js?.*");
		excludeFilenamePatterns.add(".*Vec2.js?.*");
		excludeFilenamePatterns.add(".*the_beast.js?.*");
		
		excludeFilenamePatterns.add(".*canvastext.js?.*");
		excludeFilenamePatterns.add(".*enumerable.js?.*");
		excludeFilenamePatterns.add(".*flux.js?.*");
	//	excludeFilenamePatterns.add(".*homeostasis.js?.*");
		excludeFilenamePatterns.add(".*linkage.js?.*");
		excludeFilenamePatterns.add(".*math.js?.*");
		excludeFilenamePatterns.add(".*sylvester.js?.*");
		
		excludeFilenamePatterns.add(".*design.js?.*");
	//	excludeFilenamePatterns.add(".*transform.js?.*");
		 
		excludeFilenamePatterns.add(".*help.js?.*");
		excludeFilenamePatterns.add(".*admin.js?.*");
		
		excludeFilenamePatterns.add(".*pegsDraw.js?.*");
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
				LOGGER.info("Not modifying response for " + name);
				return false;
			}
		}

		LOGGER.info("Modifying response for " + name);

		return true;
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
		System.out.println(scopename);
		if(this.noModification)
			return input;
		
		/*this line should be removed when it is used for collecting exec
		 * traces, mutating, and testing jquery library*/
	//	input = input.replaceAll("[\r\n]","\n\n");
		if (!shouldModify(scopename)) {
			return input;
		}
		
		/* this line is just for collecting exec traces from jquery while it is being used by
		 * some other application--remove it when you want to collect traces from the application itself */
	/*	if(!scopename.equals("http://localhost:8080/jquery/dist/jquery.js"))
			return input;
	*/	
	/*	if(!scopename.contains("joint.js"))
			return input;
	*/		
	/*	if(!scopename.contains("script.js"))
			return input;
	*/
		if(!scopename.contains("jquery.tiny_mce.js"))
			return input;
		try {
		
			AstRoot ast = null;

			/* initialize JavaScript context */
			Context cx = Context.enter();

			/* create a new parser */
			Parser rhinoParser = new Parser(new CompilerEnvirons(), cx.getErrorReporter());

			/* parse some script and save it in AST */
//			System.out.print(input+"*****\n");
			ast = rhinoParser.parse(new String(input), scopename, 0);

			if(this.jsModify){
		/*		BranchCvgCalc brnCvgCalc=new BranchCvgCalc(ast);
				ast.visit(brnCvgCalc);
		*/		modifier.setScopeName(scopename);

				modifier.start();

				/* recurse through AST */
				ast.visit(modifier);


				modifier.finish(ast);
			}
			else if(this.domModify){
				domModifier.setScopeName(scopename);

				domModifier.start();

				/* recurse through AST */
				ast.visit(domModifier);

				domModifier.finish(ast);
			}
			
			else if(this.domMuteModify){
				domMuteModifier.setScopeName(scopename);

				domMuteModifier.start();

				/* recurse through AST */
				ast.visit(domMuteModifier);

				domMuteModifier.finish(ast);
			}
			
			else if(this.dom_js_modify){
				dom_js_modifier.setScopeName(scopename);

				dom_js_modifier.start();

				/* recurse through AST */
				ast.visit(dom_js_modifier);

				dom_js_modifier.finish(ast);
			}
				
			/* clean up */
			Context.exit();
			System.out.print(ast.toSource()+"*****\n");
			return ast.toSource();
		} catch (RhinoException re) {
			System.err.println(re.getMessage());
			LOGGER.warn("Unable to instrument. This might be a JSON response sent"
			        + " with the wrong Content-Type or a syntax error.");
		} catch (IllegalArgumentException iae) {
			LOGGER.warn("Invalid operator exception catched. Not instrumenting code.");
		}
		LOGGER.warn("Here is the corresponding buffer: \n" + input + "\n");

		return input;
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
		
		String type = response.getHeader("Content-Type");
		System.out.println("URL:"+request.getURL().toString());
		if (request.getURL().toString().contains("?thisisanexecutiontracingcall")) {
			LOGGER.info("Execution trace request " + request.getURL().toString());
			JSExecutionTracer.addPoint(new String(request.getContent()));
			return response;
		}
		
		else if (request.getURL().toString().contains("?thisisajsdomexecutiontracingcall")) {
			LOGGER.info("Execution trace request " + request.getURL().toString());
			DOM_JS_ExecutionTracer.addPoint(new String(request.getContent()));
			return response;
		}
		
		else if(request.getURL().toString().contains("?thisisadomtracingcall")){
			LOGGER.info("Execution trace request " + request.getURL().toString());
			DOMExecutionTracer.addPoint(new String(request.getContent()));
			return response;
		}
		

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

		@Override
		public Response fetchResponse(Request request) throws IOException {
			Response response = client.fetchResponse(request);
			return createResponse(response, request);
		}
	}
	



}
