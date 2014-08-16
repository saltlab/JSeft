package units;

import java.util.ArrayList;

public class PertinentArgument {

	private static int index = 0;
	private static String baseVariable;
	private static ArrayList<String> propertyChain = new ArrayList<String>();
	
	public PertinentArgument(int i, String b) {
		this.index = i;
		this.baseVariable = b;
	}
	
	public void setIndex(int i) {
		this.index = i;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void setBase(String s) {
		this.baseVariable = s;
	}
	
	public String getBase() {
		return this.baseVariable;
	}
	
	public void addProperty(String s) {
		this.propertyChain.add(s);
	}
	
	public ArrayList<String> getProperties() {
		return this.propertyChain;
	}
	
}
