package astModifier;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.VariableDeclaration;


public class FunctionNodeFinder implements  NodeVisitor {

	public List<String> functionNodeNames=new ArrayList<String>();
	
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	@Override
	public boolean visit(AstNode node) {
		if(node instanceof FunctionNode){
			FunctionNode func=(FunctionNode) node;
			String funcName=getFunctionName(func);
			functionNodeNames.add(funcName);
		}
		return true;
	}
	
	private String getFunctionName(FunctionNode f) {
		
		if (f==null)
			return "NoFunctionNode";
	/*	else if(f.getParent() instanceof LabeledStatement){
			return ((LabeledStatement)f.getParent()).shortName();
		}
	*/	
		else if(f.getParent() instanceof ObjectProperty){
			return ((ObjectProperty)f.getParent()).getLeft().toSource();
		}
		

		else if(f.getParent() instanceof Assignment){
			AstNode funcAssignLeft=((Assignment) f.getParent()).getLeft();
			if(funcAssignLeft instanceof VariableDeclaration){
				return ((VariableDeclaration)funcAssignLeft).getVariables().get(0).toSource();
			}
			if(funcAssignLeft instanceof Name){
				return ((Name)funcAssignLeft).getIdentifier();
			}
			
			if(funcAssignLeft instanceof PropertyGet){
				if(((PropertyGet)funcAssignLeft).getLeft().toSource().equals("this")){
					String constructorName=f.getEnclosingFunction().getFunctionName().getIdentifier();
					String memberName=((PropertyGet)funcAssignLeft).getRight().toSource();
					String funcName="new " + constructorName + "()" + "." + memberName; 
					return(funcName);
				}
				
			}
				
		}
		
	
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
	}
	

}
