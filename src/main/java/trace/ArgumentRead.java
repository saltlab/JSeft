package trace;

public class ArgumentRead extends VariableRead {
    private int argumentNumber;
    private String calledFunction;
    
    public int getArgumentNumber() {
        return argumentNumber;
    }

    public void setArgumentNumber(int o) {
        argumentNumber = o;
    }
    
    public String getFunctionName() {
        return calledFunction;
    }

    public void setFunctionName(String s) {
    	calledFunction = s;
    }

}
