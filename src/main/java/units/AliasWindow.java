package units;

import trace.RWOperation;

public class AliasWindow {

	private RWOperation central;
	private RWOperation begin;
	private RWOperation end;

	// Constructor
	public AliasWindow (RWOperation me) {
		this.central = me;
	}
	
	public RWOperation getCentral() {
		return this.central;
	}
	
	public RWOperation getEnd() {
		return this.end;
	}
	
	public RWOperation getBegin() {
		return this.begin;
	}
	
	public void setCentral(RWOperation c) {
		this.central = c;
	}
	
	public void setEnd(RWOperation c) {
		this.end = c;
	}
	
	public void setBegin(RWOperation c) {
		this.begin = c;
	}
}
