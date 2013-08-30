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
				
				String funcName=""; 
				String constructorName="";
				int newcounter=0;
				while(funcAssignLeft instanceof PropertyGet && ((PropertyGet)funcAssignLeft).getLeft().toSource().equals("this")){
					newcounter++;
					
					String memberName=((PropertyGet)funcAssignLeft).getRight().toSource();
					funcName= "()"+"."+ memberName+funcName; 
				
					if(funcAssignLeft.getEnclosingFunction().getParent() instanceof Assignment){
						Assignment assign=(Assignment) funcAssignLeft.getEnclosingFunction().getParent();
						funcAssignLeft=assign.getLeft();
				
					}
					else{
						constructorName=funcAssignLeft.getEnclosingFunction().getFunctionName().getIdentifier();
						break;
					}
						
				}
				
				if(constructorName.equals("")){
					if(funcAssignLeft instanceof PropertyGet){
						constructorName=funcAssignLeft.toSource();
					
					}
					else if(funcAssignLeft instanceof VariableDeclaration){
						constructorName=((VariableDeclaration)funcAssignLeft).getVariables().get(0).toSource();
					}
				}
				String newWord="";
				for(int i=0;i<newcounter;i++)
					newWord+="new ";
				newWord+=constructorName;
				String newFuncName=newWord + funcName;
				

				return(newFuncName);
					
			}
			
	/*		if(funcAssignLeft instanceof PropertyGet){
				if(((PropertyGet)funcAssignLeft).getLeft().toSource().equals("this")){
					Name name=f.getEnclosingFunction().getFunctionName();
					if(name!=null){
						String constructorName=name.getIdentifier();
						String memberName=((PropertyGet)funcAssignLeft).getRight().toSource();
						String funcName="new " + constructorName + "()" + "." + memberName; 
						return(funcName);
					}
				}
				
			}
				
	*/	}
		
	
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
	}
	

}
