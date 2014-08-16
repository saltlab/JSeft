package instrument;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.apache.bcel.generic.INSTANCEOF;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.Symbol;

public final class InstrumenterHelper {

    public static Scope getDefiningScope(Name node) {
        // Variable information
        String variableName = node.getIdentifier();
        Scope defScope = node.getDefiningScope();

        boolean found = false;
        String[] varsInScope;

        // For scope searching
        Iterator<Scope> scopeIterator;
        ArrayList<Scope> sc = null;

        if (defScope == null) {
            // Cannot find defining scope when global var used in level 1 function for some reason
            sc = getScopeChain(node);
            scopeIterator = sc.iterator();

            while (scopeIterator.hasNext() && !found) {
                // Crawl upwards in scope chain until variable declaration found
                defScope = scopeIterator.next();

                // Checking arguments and declarations e.g. 'var x = 0;'
                varsInScope = getArgumentsAndDeclarations(defScope);

                for (int i = 0; i < varsInScope.length; i++) {
                    if (varsInScope[i].equals(variableName)) {
                        // If the variable of interest is an argument or declaration, stop searching
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                // Declaration not found after crawling scope chain, must be declared globally 
                // in another .js file
                System.out.println("[InstrumenterHelper-getDefiningScope]: Variable declaration not found for "
                        + variableName + ". Assuming global variable");
                // Go through entire AST and find all writes to variableName, then get data dependencies for each
                // write and recursively slice
            }
        }
        // returns either a function scope, or script scope
        return defScope;
    }

    public static String getScopeChainAsString(AstNode node) {
        // AstNode node should be part of a larger Ast Tree

        ArrayList<Scope> sc = getScopeChain(node);
        Iterator<Scope> it = sc.iterator();
        String returnMe = "-";
        Scope currentScope;

        while (it.hasNext()) {
            currentScope = it.next();

            if (currentScope.getType() == org.mozilla.javascript.Token.FUNCTION) {
                // Parent function
                returnMe += getFunctionNodeName((FunctionNode) currentScope);
            } else {
                // Top scope (file?)
                returnMe += "global";
            }					
        }
        System.out.println("[getScopeChainAsString]: returning " + returnMe.substring(1));

        return returnMe.substring(1);
    }

    public static ArrayList<Scope> getScopeChain(AstNode node) {
        Scope currentScope = node.getEnclosingScope();
        ArrayList<Scope> scopePath = new ArrayList<Scope>();
        
        while (currentScope != null) {
        	// Add the scope only if it is a Function or Script type, don't want blocks (i.e. 'if' statements) for now
            if (Token.typeToName(currentScope.getType()).equals("FUNCTION")
            		|| Token.typeToName(currentScope.getType()).equals("SCRIPT")) {
                scopePath.add(currentScope);
            }
            currentScope = currentScope.getEnclosingScope(); 
        }  
        return scopePath;
    }
    
    public static ArrayList<AstNode> getControlChain(AstNode node) {
    	AstNode currentControl = node.getEnclosingScope();
        ArrayList<AstNode> controlPath = new ArrayList<AstNode>();
        
        while (currentControl != null) {
        	// Add the scope only if it is a Function or Script type, don't want blocks (i.e. 'if' statements) for now
            if (!Token.typeToName(currentControl.getType()).equals("FUNCTION")
            	&& !Token.typeToName(currentControl.getType()).equals("SCRIPT")) {
            	
            	if (currentControl.getParent() != null
            			&& currentControl.getParent() instanceof IfStatement) {
                	controlPath.add(currentControl.getParent());
            	}
            }
            currentControl = currentControl.getEnclosingScope(); 
        }  
        return controlPath;
    }

    protected static String[] getArgumentsAndDeclarations(Scope scope) {
        TreeSet<String> result = new TreeSet<String>();
        Map<String, Symbol> t;

        /* get the symboltable for the current scope */
        if (scope == null) {
            return result.toArray(new String[0]);
        }

        t = scope.getSymbolTable();

        if (t != null) {
            for (String key : t.keySet()) {
                /* read the symbol */
                Symbol symbol = t.get(key);
                /* only add variables and function parameters */
                if (symbol.getDeclType() == Token.LP || symbol.getDeclType() == Token.VAR) {
                    result.add(symbol.getName());
                }
            }
        }

        return result.toArray(new String[0]);
    }

    /**
     * Returns all variables in scope.
     * 
     * @param func
     *            The function.
     * @return All variables in scope.
     */
    public static String[] getVariablesNamesInScope(Scope scope) {
        TreeSet<String> result = new TreeSet<String>();

        do {
            /* get the symboltable for the current scope */
            Map<String, Symbol> t = scope.getSymbolTable();

            if (t != null) {
                for (String key : t.keySet()) {
                    /* read the symbol */
                    Symbol symbol = t.get(key);
                    /* only add variables and function parameters */
                    if (symbol.getDeclType() == Token.LP || symbol.getDeclType() == Token.VAR) {
                        result.add(symbol.getName());
                    }
                }
            }

            /* get next scope (upwards) */
            scope = scope.getEnclosingScope();
        } while (scope != null);

        /* return the result as a String array */
        return result.toArray(new String[0]);
    }

    public static String getFunctionNodeName(FunctionNode node){
        AstNode parent = node.getParent();
        String name = node.getName();

        if (name == "" && parent.getType() == org.mozilla.javascript.Token.ASSIGN) {
            name = parent.toSource().substring(0,parent.toSource().indexOf(node.toSource()));
            name = name.substring(name.lastIndexOf(".")+1,name.indexOf("="));
        }
        return name;
    }

    public static boolean isVariableLocal(String name, FunctionNode node) {
        boolean found = false;

        String[] localVariables = getArgumentsAndDeclarations(node);

        for (int i = 0; i < localVariables.length; i++) {
            if (localVariables[i].equals(name)) {
                found = true;
                break;
            }
        }

        return found;
    }

    private InstrumenterHelper () {
    }
}
