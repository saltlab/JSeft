package executionTracer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.NodeVisitor;

public class FunctionVisitor  implements NodeVisitor{
	
	private HashSet<String> objectVars;
	private boolean objectVarFoundInScope=false;
	private HashSet<String> foundedObjectVarsList;
	private ArrayList<String> excludedList=new ArrayList<String>();
	public FunctionVisitor(HashSet<String> objectVariables){
		foundedObjectVarsList=new HashSet<String>();
		objectVarFoundInScope=false;
		objectVars=new HashSet<String>();
		objectVars=objectVariables;
		excludedList.add("send(new Array");

	}
	
	private boolean shouldVisit(AstNode node){
		 
		for(String excluded:excludedList){
			if(node.toSource().startsWith(excluded)){
				return false;
			}
			
		}
		return true;
	}
	
	
	private boolean objectVarExistsInScope(AstNode scope){
		String source=scope.toSource();
		Iterator<String> iter=objectVars.iterator();
		while(iter.hasNext()){
			String objectVar=iter.next();
			String str=objectVar.split("::")[1];
			if(str.equals(source)){
				foundedObjectVarsList.add(objectVar);
				
			}
		}
	
		return false;
	}
	
	public HashSet<String> getFoundedObjectVarsList(){
		return foundedObjectVarsList;
	}
	

	@Override
	public boolean visit(AstNode functionScope) {
		
		if(!shouldVisit(functionScope))
			return false;
		
		if(objectVarFoundInScope)
			return false;
		else 
			if(objectVarExistsInScope(functionScope)){
				objectVarFoundInScope=true;
				return false;
			}
			
		
		return true;
	}
	
	

}
