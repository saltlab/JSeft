package astModifier;

import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.VariableInitializer;
@Deprecated
public class DomNodeVisitor implements NodeVisitor {
	
	private String nodeAsVariable;
	private AstNode domNode;

	public DomNodeVisitor(String nodeAsVariable){
		this.nodeAsVariable=nodeAsVariable;
	}
	public AstNode getDomNode(){
		return domNode;
	}
	@Override
	public boolean visit(AstNode node) {
		if(node instanceof Assignment){
			if(((Assignment) node).getLeft().toSource().equals(nodeAsVariable)){
				domNode=((Assignment) node).getRight();
			}
		}
		if(node instanceof VariableInitializer)
			if(((VariableInitializer) node).getTarget().toSource().equals(nodeAsVariable))
				domNode=((VariableInitializer) node).getInitializer();
		
	return true;
	
	}
	
	

}
