package instrument.helpers;

import java.util.ArrayList;
import java.util.Iterator;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;

import instrument.InstrumenterHelper;
import units.IfStatement;

public class ControlMapper {

	private static int id = 0;
	private static ArrayList<Integer> ids = new ArrayList<Integer>();
	private static ArrayList<IfStatement> ifs = new ArrayList<IfStatement>();

	public static int getNewId() {
		return ++id;
	}

	public static int getIfId(int line, String file) {
		Iterator<IfStatement> ifIt = ifs.iterator();
		IfStatement nextIf = null;

		while (ifIt.hasNext()) {
			nextIf = ifIt.next();

			if (nextIf.contains(line, file)) {
				return nextIf.getId();
			}
		}

		// The line & file pair is not current associated with an if
		return -1;
	}

	// Return custom IfStatement instance when provided Id
	public static units.IfStatement getIf(int id) {
		Iterator<IfStatement> ifIt = ifs.iterator();
		IfStatement nextIf = null;

		while (ifIt.hasNext()) {
			nextIf = ifIt.next();

			if (nextIf.getId() == id) {
				return nextIf;
			}
		}

		return null;
	}

	public static void addIf (AstNode node, String filename) {
		// Check to see if 'if' statement already exists in records, other add new one
		ArrayList<AstNode> ctrlChain = InstrumenterHelper.getControlChain(node);
		if (ctrlChain.size() > 0) {
			Iterator<AstNode> scopeIt = ctrlChain.iterator();
			AstNode nextScope = null;
			IfStatement previousIf = null;

			while (scopeIt.hasNext()) {
				nextScope = scopeIt.next();

				if (nextScope instanceof org.mozilla.javascript.ast.IfStatement) {
					int id = getIfId(nextScope.getLineno(), filename);
					if(id != -1) {
						// Already exists, don't create new 'if' in records...just add this line to its children
						getIf(id).addChildLine(node.getLineno());
						// If a new 'If' statement has been create while traversing this chain, add its parent
						if (previousIf != null && previousIf.getParent() == null) {
							previousIf.setParent(getIf(id));
							previousIf = null;
						}	
					} else {
						// Create new 'If', add the argument line as its child
						IfStatement newIf = new IfStatement(nextScope.getLineno(), filename);
						System.out.println(newIf.getId());
						newIf.setCondition(((org.mozilla.javascript.ast.IfStatement) nextScope).getCondition());
						newIf.addChildLine(node.getLineno());
						ifs.add(newIf);
						id = newIf.getId();
						// If a new 'If' statement has been create while traversing this chain, add its parent
						if (previousIf != null && previousIf.getParent() == null) {
							previousIf.setParent(newIf);
						}	
						previousIf = getIf(id);
					}

				} 
			}
		}
	}

}
