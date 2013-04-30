package executionTracer;



import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;

public class KeywordVisitor implements  NodeVisitor{

	private boolean hasThisKeywords=false;
	
	public KeywordVisitor(){
		hasThisKeywords=false;
	}
	@Override
	public boolean visit(AstNode node) {
		
		if(node instanceof KeywordLiteral){
			if(((KeywordLiteral)node).getType()==Token.THIS){
				hasThisKeywords=true;
				return false;
			}
		}
		return true;
	}
	
	public boolean getHasThisKeyword(){
		return hasThisKeywords;
	}



}
