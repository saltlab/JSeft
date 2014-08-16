package trace;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class RWOperation implements Comparable<RWOperation> {

    private int order;
    private boolean partOfSlice = false;
    private int lineNo;
    private String variable;
    private String file;
    //private int messageType;
    private RWOperation parentOperation;
    private ArrayList<RWOperation> childenOperations = new ArrayList<RWOperation>();

    public int compareTo(RWOperation arg0) {
        if (order < arg0.getOrder()) {
            return -1;
        } else if (order > arg0.getOrder()) {
            return 1;
        }
        return 0;
    }

    public int getOrder() {
        return order;
    }
    public void setOrder(int o) {
        order = o;
    }

    public int getLineNo() {
        return lineNo;
    }
    public void setLineNo(int o) {
        lineNo = o;
    }

    public String getVariable() {
        return variable;
    }
    public void setVariable(String o) {
        variable = o;
    }

    public void addChild (RWOperation c) {
        this.childenOperations.add(c);
    }
    public ArrayList<RWOperation> getChildren() {
        return this.childenOperations;
    }
    public void clearChildren() {
        this.childenOperations = new ArrayList<RWOperation>();
    }

    public void setParent(RWOperation p) {
        this.parentOperation = p;
    }
    public RWOperation getParent(){
        return this.parentOperation;
    }
    
    public void includeInSlice () {
        this.partOfSlice = true;
    }
    public void omitFromSlice () {
        this.partOfSlice = false;
    }
    public boolean getSliceStatus () {
    	return this.partOfSlice;
    }
    
    public String getFile() {
        return this.file;
    }
    public void setFile (String f) {
    	this.file = f;
    }
}
