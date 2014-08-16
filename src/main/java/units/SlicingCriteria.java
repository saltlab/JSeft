package units;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Scope;

public class SlicingCriteria {

	private Scope startScope;
	private String variableName;
	private boolean interTracking = false;
	
	public SlicingCriteria(Scope s, String n, boolean i) {
		this.startScope = s;
		this.variableName = n;
		this.interTracking = i;
	}
	
	public Scope getScope () {
		return startScope;
	}
	
	public String getScopeName () {
		String returnMe = "global";
		
		if (startScope instanceof FunctionNode) {
			FunctionNode node = (FunctionNode) startScope;
			
			String name = node.getName();
			AstNode parent = node.getParent();
			
			if (node.getFunctionType() == FunctionNode.FUNCTION_EXPRESSION) {
	            // Complicated Case
	            if (node.getName() == "" && parent.getType() == org.mozilla.javascript.Token.COLON) {
	                // Assignment Expression                    
	                name = node.getParent().toSource().substring(0,node.getParent().toSource().indexOf(node.toSource()));
	                name = name.substring(0,name.indexOf(":"));
	            } else if (node.getName() == "" && parent.getType() == org.mozilla.javascript.Token.ASSIGN) {
	                name = node.getParent().toSource().substring(0,node.getParent().toSource().indexOf(node.toSource()));
	                name = name.substring(name.lastIndexOf(".")+1,name.indexOf("="));
	            }
	            name = name.trim();
	        }
			
			returnMe = name;
		} else {
			System.out.println("[SlicingCriteria  getScopeName]: " + startScope.getClass());
		}
		
		return returnMe;
	}
	
	public String getVariable () {
		return variableName;
	}
	
	public void setInter (boolean t) {
		this.interTracking = t;
	}
	
	public boolean getInter () {
		return this.interTracking;
	}
	
	public boolean equals (SlicingCriteria compareTo) {
		if (compareTo.getVariable().indexOf(variableName) > -1
				&& compareTo.getScope().equals(startScope)) {
			return true;
		} else {
			return false;
		}
	}
}
