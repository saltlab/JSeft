package astModifier;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ast.AstRoot;

import mutandis.analyser.JSCyclCompxCalc;
import mutandis.astModifier.JSASTModifier;
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
	
	public JSModifyProxyPluginforCodeMutation(String outputfolder){
		super(outputfolder);
		
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



}
