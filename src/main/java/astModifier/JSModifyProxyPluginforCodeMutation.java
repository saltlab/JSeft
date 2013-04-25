package astModifier;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ast.AstRoot;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.util.Helper;

import executionTracer.DOM_JS_ExecutionTracer;
import executionTracer.JSExecutionTracer;

import mutandis.analyser.JSCyclCompxCalc;
import mutandis.astModifier.JSASTModifier;
import mutandis.exectionTracer.JSFuncExecutionTracer;
import mutandis.exectionTracer.JSVarExecutionTracer;
import mutandis.mutator.BranchVisitor;
import mutandis.mutator.NodeMutator;
import mutandis.mutator.VariableVisitor;

public class JSModifyProxyPluginforCodeMutation extends mutandis.astModifier.JSModifyProxyPlugin {

	private astModifier.JSASTModifier modifierToGetMutatedVerInfo;
	public JSModifyProxyPluginforCodeMutation(JSASTModifier modify, JSCyclCompxCalc cyclCompxCalc, astModifier.JSASTModifier modifierToGetMutatedVerInfo) {
		
		super(modify,cyclCompxCalc);
		this.modifierToGetMutatedVerInfo=modifierToGetMutatedVerInfo;
	}

	
	public JSModifyProxyPluginforCodeMutation(JSASTModifier modify, List<String> excludes,JSCyclCompxCalc cyclCompxCalc,  astModifier.JSASTModifier modifierToGetMutatedVerInfo) {
		super(modify,excludes,cyclCompxCalc);
		this.modifierToGetMutatedVerInfo=modifierToGetMutatedVerInfo;
	}
	
	public JSModifyProxyPluginforCodeMutation(String outputfolder,  astModifier.JSASTModifier modifierToGetMutatedVerInfo){
		super(outputfolder);
		this.modifierToGetMutatedVerInfo=modifierToGetMutatedVerInfo;
		
	}
	
	
	@Override
	protected synchronized String modifyJS(String input, String scopename) {
		
		/*this line should be removed when it is used for collecting exec
		 * traces, mutating, and testing jquery library*/
		input = input.replaceAll("[\r\n]","\n\n");
		if (!shouldModify(scopename)) {
			return input;
		}
		
		/* this line is just for collecting exec traces from jquery while it is being used by
		 * some other application--remove it when you want to collect traces from the application itself */
	/*	if(!scopename.equals("http://localhost:8080/jquery/dist/jquery.js"))
			return input;
	*/	
		else
			if(singleMutationDone)
				return input;
		
		try {
		
			AstRoot ast = null;	
			
			/* initialize JavaScript context */
			Context cx = Context.enter();

			/* create a new parser */
			Parser rhinoParser = new Parser(new CompilerEnvirons(), cx.getErrorReporter());
			
			/* parse some script and save it in AST */
			ast = rhinoParser.parse(new String(input), scopename, 0);

			if(shouldGetInfoFromCode){
			
				cyclCompxCalc.setScopeName(scopename);
				ast.visit(cyclCompxCalc);
				cyclCompxCalc.finish();
			
				modifier.setScopeName(scopename);
				modifier.start();

			/* recurse through AST */
				modifier.shouldTrackFunctionNodes=true;
				ast.visit(modifier);
			
				if(modifier.shouldTrackFunctionCalls){
					modifier.shouldTrackFunctionNodes=false;
					ast.visit(modifier);
				}
				

				modifier.finish(ast);
			}
			else{
				
					if(shouldStartNonJsMutatations){
					
					funcNodeVisitor.setScopeName(scopename);
					ast.visit(funcNodeVisitor);
					/* mutating variables */
					if(funcNodeVisitor.getIsVariableMut()){
						VariableVisitor varvis=funcNodeVisitor.getVariableVisitor();
						if(varvis.getVariableMap().size()!=0){
							List<Object> desiredList=varvis.getRandomVariableMap();
		
							NodeMutator nm=new NodeMutator(outputfolder,scopename);
							singleMutationDone=nm.mutateVariable(desiredList);
			
						}
						else{
							NodeMutator nm=new NodeMutator(outputfolder,scopename);
							StringBuffer stb=new StringBuffer();
							stb.append("no changes made to the code"+"\n");
							stb.append("================"+"\n");
							nm.writeResultsToFile(stb.toString());
						}
						
					}
					/* mutating branches */
					else if(!funcNodeVisitor.getIsVariableMut()){
						BranchVisitor brvis=funcNodeVisitor.getBranchVisitor();
						if(brvis.getBranchMap().size()!=0){
							List<Object> desiredList=brvis.getRandomBranchMap();
							NodeMutator nm=new NodeMutator(outputfolder,scopename);
							singleMutationDone=nm.mutateBranchStatements(desiredList);
						}
						else{
							NodeMutator nm=new NodeMutator(outputfolder,scopename);
							StringBuffer stb=new StringBuffer();
							stb.append("no changes made to the code"+"\n");
							stb.append("================"+"\n");
							nm.writeResultsToFile(stb.toString());
						}
					}
					
					
				}
				else if(shouldStartJsSpecMutations){
					jsSpecVisitor.setScopeName(scopename);
					ast.visit(jsSpecVisitor);
					jsSpecVisitor.setJsSpecList();
					List<Object> desiredList=jsSpecVisitor.getElementfromJsSpecList(indexOfJsSpecToVisit);
					if(desiredList!=null){
						NodeMutator nm=new NodeMutator(outputfolder,scopename);
						singleMutationDone=nm.mutateJsSpecfic(desiredList);
					}
					else{
						NodeMutator nm=new NodeMutator(outputfolder,scopename);
						StringBuffer stb=new StringBuffer();
						stb.append("no changes made to the code"+"\n");
						stb.append("================"+"\n");
						nm.writeResultsToFile(stb.toString());
					}
					
				}
				else if(shouldStartDomJsMutations){
				
					domJsVisitor.setScopeName(scopename);
					ast.visit(domJsVisitor);
					domJsVisitor.setJsDomList();
					List<Object> desiredList=domJsVisitor.getElementfromJsDomList(indexOfJsDomToVisit);
					if(desiredList!=null){
						NodeMutator nm=new NodeMutator(outputfolder,scopename);
						singleMutationDone=nm.mutateDomJsCodeLevel(desiredList);
					}
					else{
						NodeMutator nm=new NodeMutator(outputfolder,scopename);
						StringBuffer stb=new StringBuffer();
						stb.append("no changes made to the code"+"\n");
						stb.append("================"+"\n");
						nm.writeResultsToFile(stb.toString());
					}
				}
					
				modifierToGetMutatedVerInfo.setScopeName(scopename);

				modifierToGetMutatedVerInfo.start();
				
				/* recurse through AST */
				ast.visit(modifierToGetMutatedVerInfo);	

				modifierToGetMutatedVerInfo.finish(ast);
			}
			
			
			/* clean up */
			Context.exit();
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
	
	@Override
	protected Response createResponse(Response response, Request request) {
		String type = response.getHeader("Content-Type");
		
		if (request.getURL().toString().contains("?thisisanexecutiontracingcall")) {
			LOGGER.info("Execution trace request " + request.getURL().toString());
			JSExecutionTracer.addPoint(new String(request.getContent()));
			return response;
		}

		if (request.getURL().toString().contains("?thisisavarexectracingcall")) {
			LOGGER.info("Execution trace request " + request.getURL().toString());
			JSVarExecutionTracer.addPoint(new String(request.getContent()));
			return response;
		}
		if (request.getURL().toString().contains("?thisisafuncexectracingcall")){
			
			LOGGER.info("Execution trace request " + request.getURL().toString());
			JSFuncExecutionTracer.addPoint(new String(request.getContent()));
			return response;
		}
		
		if (request.getURL().toString().contains("?thisisajsdomexecutiontracingcall")) {
			LOGGER.info("Execution trace request " + request.getURL().toString());
			DOM_JS_ExecutionTracer.addPoint(new String(request.getContent()));
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



}
