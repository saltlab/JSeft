package configuration;

import java.util.ArrayList;
import java.util.Iterator;

import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Scope;

import trace.ArgumentRead;
import trace.ArgumentWrite;
import trace.FunctionEnter;
import trace.PropertyRead;
import trace.RWOperation;
import trace.ReturnStatementValue;
import trace.ReturnValueWrite;
import trace.VariableRead;
import trace.VariableWrite;
import instrument.ProxyInstrumenter2;

public class TraceHelper {

	public static int getIndexOfIgnoreOrderNumber(ArrayList<RWOperation> trace, RWOperation findMe){
		int i = 0;
		Iterator<RWOperation> it = trace.iterator();
		RWOperation next;

		while (it.hasNext()) {
			next = it.next();

			if (next.getClass().equals(findMe.getClass())
					&& next.getLineNo() == findMe.getLineNo()
					&& next.getVariable().equals(findMe.getVariable())) {
				return i;
			}

			i++;
		}
		return -1;
	}

	public static ArrayList<RWOperation> getConditionalReads(ArrayList<RWOperation> trace, RWOperation priorToMe, int ifStmtLine) {
		ArrayList<RWOperation> returnMe = new ArrayList<RWOperation>();

		for (int i = trace.indexOf(priorToMe); i >= 0; i--) {
			// Going backwards
			if (trace.get(i).getLineNo() == ifStmtLine && trace.get(i) instanceof VariableRead) {
				// Add reads on the same line as the if statement (assuming these are within the if condition for now)
				returnMe.add(trace.get(i));
			} else if (trace.get(i).getLineNo() < ifStmtLine) {
				// Not sure about this, needs more testing
				break;
			}
		}

		return returnMe;
	}

	public static FileLineNumber getFileLineMapping(String fileName, ArrayList<FileLineNumber> a) {
		Iterator<FileLineNumber> ir = a.iterator();
		FileLineNumber next;

		while (ir.hasNext()) {
			next = ir.next();

			if (next.getFileName().equals(fileName)) {
				return next;
			}
		}
		return null;
	}

	// OBSOLETE
	public static RWOperation getElementAtIndex(ArrayList<RWOperation> trace, int index) throws IndexOutOfBoundsException {
		int i = 0;
		Iterator<RWOperation> it = trace.iterator();
		RWOperation next;

		while (it.hasNext()) {
			next = it.next();

			if (i == index) {
				return next;
			}
			i++;
		}

		System.err.println("Invalid index when searching trace.");
		throw new IndexOutOfBoundsException();
	}

	public static ArrayList<RWOperation> getDataDependencies(ArrayList<RWOperation> trace, VariableWrite vw) throws Exception {
		int i = trace.indexOf(vw);
		ArrayList<RWOperation> deps = new ArrayList<RWOperation>();
		RWOperation current;
		boolean jumpAllowed = false;

		for (int j = i - 1; j >= 0; j--) {
			current = trace.get(j);

			// TODO: Might need better criteria for checking if write is dependent on read
			// (programmer might split assignment operation between mutliple lines)
			if (current instanceof VariableRead && current.getLineNo() == vw.getLineNo()) {
				deps.add(current);
				jumpAllowed = false;
			} /*else if (current instanceof PropertyRead && current.getLineNo() == vw.getLineNo()) {
				j = getAtomicIndex(trace, (PropertyRead) current);
				deps.add(current);
				jumpAllowed = false;
			} */else if (current instanceof ReturnStatementValue) {
				// Line number is allow to change (!= vw.getLineNo())


				j = trace.indexOf(getBeginningOfFunction((ReturnStatementValue) current, trace));


				// set a flag?
				jumpAllowed = true;

			} else if (current.getLineNo() != vw.getLineNo() && !jumpAllowed) {
				break;
			}
		}
		return deps;
	}
	
	public static ArrayList<RWOperation> getDataDependenciesLoose(ArrayList<RWOperation> trace, RWOperation vw) throws Exception {
		int i = trace.indexOf(vw);
		ArrayList<RWOperation> deps = new ArrayList<RWOperation>();
		RWOperation current;

		for (int j = i; j < trace.size(); j++) {
			current = trace.get(j);

			if (current instanceof VariableRead && current.getLineNo() == vw.getLineNo()) {
				deps.add(current);
			} else if (current.getLineNo() != vw.getLineNo()) {
				break;
			}
		}
		return deps;
	}

	public static Scope getDefiningScope(AstRoot ast, String name, int lineNo) {
		ProxyInstrumenter2 sc = new ProxyInstrumenter2();

		sc.setLineNo(lineNo);
		sc.setVariableName(name); 

		ast.visit(sc);

		System.out.println(name);
		System.out.println(lineNo);

		if (sc.getLastScopeVisited() != null) {
			System.out.println(sc.getLastScopeVisited().getLineno());
		}

		return sc.getLastScopeVisited();
	}

	public static int getAtomicIndex (ArrayList<RWOperation> trace, PropertyRead pr) throws Exception {
		int i = trace.indexOf(pr);
		String base;
		String base2;
		String prop;
		String prop2;
		String[] properties;
		String[] properties2;
		PropertyRead previousPropRead = pr;

		for (int j = i - 1; j >= 0; j--){
			if (trace.get(j) instanceof PropertyRead) {
				base = ((PropertyRead) trace.get(j)).getVariable();
				prop = ((PropertyRead) trace.get(j)).getProperty();
				properties = prop.split("\\.");

				for (int k = 0; k < properties.length; k++) {
					if (properties[k].indexOf("(") != -1) {
						properties[k] = properties[k].substring(0, properties[k].indexOf("("));
					}
					base += "."+properties[k];
				}

				prop2 = previousPropRead.getProperty();
				properties2 = prop2.split("\\.");
				base2 = previousPropRead.getVariable();
				for (int k = 0; k < properties2.length - 1; k++) {
					if (properties2[k].indexOf("(") != -1) {
						properties2[k] = properties2[k].substring(0, properties2[k].indexOf("("));
					}
					base2 += "."+properties2[k];
				}

				// if (!base2.equals(base)) {
				if (base2.indexOf(base) != 0) {
					// Same operation still
					System.out.println(base);
					System.out.println(base2);
					throw new Exception();
				}
			} else if (trace.get(j) instanceof VariableRead) {
				return j;
			} else {
				System.out.println("[TraceHelper.getAtomicIndex]: Invalid RWOperation instead of PropertyRead/VariableRead.");
				throw new Exception();
			}
		}
		return i;
	}

	public static boolean isComplex(String value) {
		// Aliases are irrelevant if the assigned type is primitive
		if (value.equals("[object Number]")
				|| value.equals("[object String]")
				|| value.equals("[object Null]")
				|| value.equals("[object Undefined]")) {
			return false;
		}
		return true;
	}

	public static RWOperation getEndOfFunction(ArgumentWrite enter, ArrayList<RWOperation> trace) {
		// PRE: ArgumentRead enter (first argument) must have an ArgumentWrite succeeding it for guaranteed 
		//      correct behavior
		RWOperation next;
		int depth = -1;

		for (int i = trace.indexOf(enter); i < trace.size(); i++) {
			next = trace.get(i);

			if (next instanceof ReturnStatementValue
					&& enter.getFunctionName().equals(((ReturnStatementValue) next).getFunctionName())) {
				if (depth == 0) {
					return next;
				} else {
					depth--;
				}
			} else if (next instanceof ArgumentWrite
					&& enter.getFunctionName().equals(((ArgumentWrite) next).getFunctionName())) {
				// Group or single 'ArgumentWrite' means a function is entered
				int j;
				for (j = i; j < trace.size(); j++) {
					if (!(trace.get(j) instanceof ArgumentWrite)) {
						break;
					}
				}
				depth++;
				// Continue search after the arguments have been initialized
				i = j - 1;
			}
		}

		return null;
	}

	public static boolean isReadAsynchronous (int currentPosition, String definingFn, ArrayList<RWOperation> trace) {
		RWOperation next;

		for (int i = currentPosition; i >= 0; i--) {
			next = trace.get(i);

			if (next instanceof ArgumentWrite && ((ArgumentWrite) next).getFunctionName().equals(definingFn)) {
				// Entrance to defining function has been hit
				return false;
			} else if (next instanceof ReturnStatementValue && ((ReturnStatementValue) next).getFunctionName().equals(definingFn)) {
				// An exit from the defining variable has been hit
				return true;
			}
		}

		// The function was not encountered, assume asynchronous
		return true;		
	}
	
	public static int getEndOfLastFnInstance (int currentPosition, String definingFn, ArrayList<RWOperation> trace) {
		RWOperation next;

		for (int i = currentPosition; i >= 0; i--) {
			next = trace.get(i);

			if (next instanceof ReturnStatementValue && ((ReturnStatementValue) next).getFunctionName().equals(definingFn)) {
				// An exit from the defining variable has been hit
				return i;
			}
		}

		// The function was not encountered, assume asynchronous
		return -1;		
	}

	public static RWOperation getBeginningOfFunction(ReturnStatementValue exit, ArrayList<RWOperation> trace) {
		RWOperation next;
		int depth = -1;

		for (int i = trace.indexOf(exit); i >= 0; i--) {
			next = trace.get(i);

			if (next instanceof ReturnStatementValue
					&& exit.getFunctionName().equals(((ReturnStatementValue) next).getFunctionName())) {
				depth++;
			} else if (next instanceof ArgumentWrite
					&& exit.getFunctionName().equals(((ArgumentWrite) next).getFunctionName())) {
				// Group or single 'ArgumentWrite' means a function is entered
				int j;
				for (j = i; j > 0; j--) {
					if (!(trace.get(j) instanceof ArgumentWrite)) {
						break;
					}
				}
				if (depth == 0) {
					return trace.get(j);
				}
				// Continue search for entrance to function
				depth--;
				i = j + 1;
			}
		}
		return null;
	}

	public static ArrayList<String> getReturnDependencies(ArrayList<RWOperation> trace, ReturnStatementValue rs) {
		ArrayList<String> names = new ArrayList<String>();

		//TODO:

		return names;
	}


}
