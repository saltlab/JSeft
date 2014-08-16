package units;

import java.util.ArrayList;
import java.util.Iterator;

import org.mozilla.javascript.ast.AstNode;

import instrument.helpers.ControlMapper;

public class IfStatement {

	private IfStatement parent = null;

	// If statement fingerprint
	private int lineNo = -1;
	private String file = "";
	private int id = -1;
	private AstNode condition = null;

	private ArrayList<Integer> childlines = new ArrayList<Integer>();

	// Constructor
	public IfStatement(int lineNo, String name) {
		this.lineNo = lineNo;
		this.file = name;
		this.id = ControlMapper.getNewId();
	}
	
	public int getId() {
		return this.id;
	}
	
	public int getLine () {
		return this.lineNo;
	}

	public IfStatement getParent () {
		return parent;
	}

	public void setParent(IfStatement p) {
		this.parent = p;
	}
	
	public AstNode getCondition () {
		return this.condition;
	}
	
	public void setCondition (AstNode c) {
		this.condition = c;
	}
	
	public void addChildLine(int line) { 
		this.childlines.add(line);
	}

	public boolean contains(int line, String file) {
		// Only those lines in the same JavaScript file can be a child
		System.out.println("If statement file: " + this.file);
		System.out.println("Child line file: " + file);
		if (file.equals(this.file)) {
			Iterator<Integer> it = childlines.iterator();
			int nextChild = -1;

			// Check all registered lines/children
			while (it.hasNext()) {
				nextChild = it.next();
				// The line has been registered as a child during our static analysis
				if (nextChild == line) {
					return true;
				}
			}
		}

		// If the line not a child OR if the file is different, return false
		return false;
	}

}
