package executionTracer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.Scope;

import executionTracer.AstInstrumenter.variableUsageType;

public class VisitObjectTypeVars implements NodeVisitor{
	
	private HashSet<String> objectVars;
	private String varUsage;
	private final Scope scope;
	private ArrayList<String> excludedList=new ArrayList<String>();
	
	public VisitObjectTypeVars(String varUsage, Scope scope){
		objectVars=new HashSet<String>();
		this.varUsage=varUsage;
		this.scope=scope;
		excludedList.add("send(new Array");
		excludedList.add("var instrumentationArray");
		
	}
	
	private boolean shouldVisit(AstNode node){
	 
		for(String excluded:excludedList){
			if(node.toSource().startsWith(excluded)){
				return false;
			}
			
		}
		return true;
	}

	@Override
	public boolean visit(AstNode node) {
		if(!shouldVisit(node))
			return false;
		
		if(varUsage.equals(variableUsageType.global.toString())
				&& node.toSource().equals(scope.toSource())){
			return false;
		}
		
		
		if(!node.toSource().equals(scope.getAstRoot().toSource()) && (node.getEnclosingScope()!=null &&!node.getEnclosingScope().toSource().contains(this.scope.toSource()))){
			return false;
		}
		if(node instanceof Name && node.getParent() instanceof PropertyGet 
				&& !(node.getParent().getParent() instanceof FunctionCall) && !node.getParent().toSource().contains("function")){
			
			objectVars.add(varUsage + "::" + node.getParent().toSource().replaceAll("\\[.*\\]", ""));
			
		
		}
		return true;
		
	}
	
	public HashSet<String> getObjectVars(){
		if(varUsage.equals(variableUsageType.global.toString())
				|| varUsage.equals(variableUsageType.returnVal.toString())){
			if(objectVars.size()!=0){
				FunctionVisitor funcVis=new FunctionVisitor(objectVars);
				this.scope.visit(funcVis);
				HashSet<String> foundedObjectVars=funcVis.getFoundedObjectVarsList();
				return foundedObjectVars; 
			}
		}
		return objectVars;
	}

}
