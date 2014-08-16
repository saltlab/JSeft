package configuration;

import java.util.ArrayList;

public class FileLineNumber {

    private String fileName = "";
    private ArrayList<Integer> lines = new ArrayList<Integer>();
    private ArrayList<String> lvl2Functions = new ArrayList<String>();
    
    public void addLevel2FunctionName(String n) {
    	this.lvl2Functions.add(n);
    }
    
    public ArrayList<String> getLevel2FunctionNames() {
    	return this.lvl2Functions;
    }

    public FileLineNumber(String f) {
        this.fileName = f;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public boolean hasLine(int i) {
        return (lines.indexOf(i) != -1);
    }
    
    public ArrayList<Integer> getLines() {
        return this.lines;
    }
    
    public void addLine(int i) {
        // No duplicates
        if (!hasLine(i)) {
            this.lines.add(i);
        }
    }
    
    public String getLinesAsString() {
        String dataLines = "";

        for (int a = 0; a < lines.size(); a++) {
            dataLines += (lines.get(a)+1)+",";
        }
        
        return dataLines;
    }

}
