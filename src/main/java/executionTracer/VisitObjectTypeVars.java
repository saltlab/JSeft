package executionTracer;

import java.util.HashSet;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;

public class VisitObjectTypeVars implements NodeVisitor{
	
	private HashSet<String> objectVars;
	
	public VisitObjectTypeVars(){
		objectVars=new HashSet<String>();
	}

	@Override
	public boolean visit(AstNode node) {
		
		if(node instanceof Name && node.getParent() instanceof PropertyGet
				&& !(node.getParent().getParent() instanceof FunctionCall) && !node.getParent().toSource().contains("function")){
			
			objectVars.add(node.getParent().toSource());
			
		}
		return true;
	}
	
	public HashSet<String> getObjectVars(){
		return objectVars;
	}

}
