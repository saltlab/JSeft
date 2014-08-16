package example;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.Scope;

import com.google.common.io.Resources;

import instrument.AstInstrumenter;
import instrument.DependencyFinder;
import instrument.FunctionCallerDependencies;
import instrument.FunctionDeclarationFinder;
import instrument.FunctionDeclarationInstrumenter;
import instrument.InstrumenterHelper;
import instrument.ProxyInstrumenter2;
import instrument.ReadWriteReplacer;
import units.ArgumentPassedIn;
import units.FunctionArgumentPair;
import units.SlicingCriteria;

public class LocalExample {

    //private static String targetFile = "/short_bunnies.js";
    private String targetFile = "";
    private int tempLineNo = -1;
    private String varName = "";

    // Definition scope finder

    // SCOPE FIND
    private static ProxyInstrumenter2 sc = new ProxyInstrumenter2();

    private static ArrayList<SlicingCriteria> remainingSlices = new ArrayList<SlicingCriteria>();
    private static ArrayList<SlicingCriteria> remainingSlices2 = new ArrayList<SlicingCriteria>();
    private static ArrayList<SlicingCriteria> completedSlices = new ArrayList<SlicingCriteria>();
    private static ArrayList<FunctionArgumentPair> declarationsToInstrument = new ArrayList<FunctionArgumentPair>();
    private static ArrayList<String> callsToInstrument = new ArrayList<String>();
    private static ArrayList<Integer> argumentsToInstrument = new ArrayList<Integer>();

    public void main(String[] args) {
        URL urlOfTarget = AstInstrumenter.class.getResource(targetFile);
        String jsFileContents;

        System.out.println("[LocalExample]: ");
        System.out.println(urlOfTarget);

        try {
            jsFileContents = Resources.toString(urlOfTarget, Charset.defaultCharset());
            instrument(jsFileContents, targetFile);
        } catch (IOException e) {
            System.out.println("[LocalExample]: " + "Trouble reading local file.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("[LocalExample]: " + "Something went wrong while instrumenting code.");
            e.printStackTrace();
        }
    }

    public void setTargetFile (String file) { 
        targetFile = "/" + file;
    }

    public void setLineNo (int l) { 
        tempLineNo = l;
    }

    public void setVariableName (String n) { 
        varName = n;
    }

    public String instrument(String input, String scopename) throws Exception {



        boolean shortInstr = false;

        // Not the start file, only look for funciton declarations and global variables
        if (!scopename.equals(this.targetFile)) {
            shortInstr = true;
        }

        AstRoot ast = null;
        Scope scopeOfInterest = null;

        Iterator<SlicingCriteria> itsc2;
        Name start = new Name();
        SlicingCriteria justFinished;
        ArrayList<AstNode> varDeps;
        FunctionDeclarationFinder fdf;

        // TODO: TESTING THIS
        ArrayList<ArgumentPassedIn> logTheseArguments = new ArrayList<ArgumentPassedIn>();

        Scope definingScope;

        ArrayList<SlicingCriteria> possibleNextSteps = new ArrayList<SlicingCriteria>();
        Iterator<AstNode> it;
        AstNode step;

        /* initialize JavaScript context */
        Context cx = Context.enter();

        /* create a new parser */
        CompilerEnvirons compilerEnvirons =  new CompilerEnvirons();
        compilerEnvirons.setRecordingLocalJsDocComments(true);
        compilerEnvirons.setAllowSharpComments(true);
        compilerEnvirons.setRecordingComments(true);
        compilerEnvirons.setOptimizationLevel(0);
        Parser rhinoParser = new Parser(compilerEnvirons, cx.getErrorReporter());

        /* parse some script and save it in AST */
        ast = rhinoParser.parse(new String(input), scopename, 0);

        // First criteria specified by user?
        start.setIdentifier(varName);
        start.setLineno(tempLineNo);
        definingScope = getDefiningScope(ast, start);

        System.out.println("[LocalExample]:  " + scopename);
        System.out.println("[definingScope]:  " + definingScope);
        System.out.println(varName);
        System.out.println(tempLineNo);
        System.out.println(this.targetFile);

        if (definingScope != null) {
            remainingSlices.add(new SlicingCriteria(definingScope, varName, true));
        }

        if (shortInstr == true) {

            // Global
            for (int zr = 0; zr < remainingSlices2.size(); zr++) {
                if (remainingSlices2.get(zr).getScope().getType() == org.mozilla.javascript.Token.SCRIPT) {
                    possibleNextSteps.add(remainingSlices2.get(zr));
                }
            }

            // Function declarations (global?)
            fdf = new FunctionDeclarationFinder();
            for (int jj = 0; jj < declarationsToInstrument.size(); jj++) {
                fdf.setFunctionArgumentPair(declarationsToInstrument.get(jj));

                ast.visit(fdf);

                ArrayList<AstNode> asd = fdf.getArgumentsNode();
                Iterator<AstNode> itt = asd.iterator();
                AstNode next;

                while (itt.hasNext()) {
                    next = itt.next();

                    if (next instanceof Name) {
                        possibleNextSteps.add(new SlicingCriteria(getDefiningScope(ast, (Name) next), ((Name) next).getIdentifier(), false));
                    } 					
                }
            }
            addToQueue(possibleNextSteps);

            // Function calls to global functions

            possibleNextSteps = new ArrayList<SlicingCriteria>();
            for (int jj = 0; jj < callsToInstrument.size(); jj++) {
                FunctionCallerDependencies fcd = new FunctionCallerDependencies();
                fcd.setFunctionName(callsToInstrument.get(jj));
                fcd.setArgumentNumber(argumentsToInstrument.get(jj));
                System.out.println(callsToInstrument.get(jj));
                System.out.println(argumentsToInstrument.get(jj));
                System.out.println(scopename);
                //fcd.setTopScope(ast);          
                fcd.clearDataDependencies();
                fcd.setScopeName(scopename);
                ast.visit(fcd); 

                ArrayList<AstNode> varPassedAsArg = fcd.getDataDependencies();
                for (int u = 0; u < varPassedAsArg.size(); u++) {
                    possibleNextSteps.add(new SlicingCriteria(getDefiningScope(ast, (Name) varPassedAsArg.get(u)), ((Name) varPassedAsArg.get(u)).getIdentifier(), false));
                    System.out.println(getDefiningScope(ast, (Name) varPassedAsArg.get(u)).toSource());
                    System.out.println(((Name) varPassedAsArg.get(u)).getIdentifier());
                }

            }
            addToQueue(possibleNextSteps);


        }

        while (remainingSlices.size() > 0) {
            varDeps = new ArrayList<AstNode>();
            possibleNextSteps = new ArrayList<SlicingCriteria>();

            justFinished = new SlicingCriteria(remainingSlices.get(0).getScope(), remainingSlices.get(0).getVariable(), remainingSlices.get(0).getInter());

            // "UPWARDS"
            if (justFinished.getScope() instanceof FunctionNode 
                    && isArgument(justFinished.getVariable(), justFinished.getScope()) > -1) {
                // Need to find all places where the function is called add argument number __ as a data dependency

                // Shouldn't be any duplicates at this point...remainingSlices are all unique?
                logTheseArguments.add(new ArgumentPassedIn((FunctionNode) justFinished.getScope(), justFinished.getVariable(), isArgument(justFinished.getVariable(), justFinished.getScope())));

                FunctionCallerDependencies fcd = new FunctionCallerDependencies();




                FunctionNode lookForMyCalls = ((FunctionNode) justFinished.getScope());
                AstNode parent = lookForMyCalls.getParent();
                String name = ((FunctionNode) justFinished.getScope()).getName();

                if (lookForMyCalls.getFunctionType() == FunctionNode.FUNCTION_EXPRESSION) {
                    // Complicated Case
                    if (lookForMyCalls.getName() == "" && parent.getType() == org.mozilla.javascript.Token.COLON) {
                        // Assignment Expression                    
                        name = lookForMyCalls.getParent().toSource().substring(0,parent.toSource().indexOf(lookForMyCalls.toSource()));
                        name = name.substring(0,name.indexOf(":"));
                    } else if (lookForMyCalls.getName() == "" && parent.getType() == org.mozilla.javascript.Token.ASSIGN) {
                        name = parent.toSource().substring(0, parent.toSource().indexOf(lookForMyCalls.toSource()));
                        name = name.substring(name.lastIndexOf(".")+1,name.indexOf("="));
                    }
                    name = name.trim();
                } 
                fcd.setFunctionName(name);
                if (callsToInstrument.indexOf(name) == -1) {
                    callsToInstrument.add(name);
                    argumentsToInstrument.add(isArgument(justFinished.getVariable(), justFinished.getScope()));
                }

                fcd.setArgumentNumber(isArgument(justFinished.getVariable(), justFinished.getScope()));
                fcd.setTopScope(((FunctionNode) justFinished.getScope()).getEnclosingScope());          
                fcd.clearDataDependencies();
                fcd.setScopeName(targetFile);

                fcd.getTopScope().visit(fcd);   

                if (justFinished.getInter() == true) {
                    varDeps.addAll(fcd.getDataDependencies());


                    for (int pp = 0; pp < fcd.getDataDependencies().size(); pp++) {
                        // variable arguments passed to this function of interest elsewhere
                        System.out.println(Token.typeToName(fcd.getDataDependencies().get(pp).getType()));
                    }


                }		// get enclosing scope of the function delcaration...and find all calls to the function in there
            }

            // Get next variables dependencies
            varDeps.addAll(getDataDependencies(ast, justFinished));


            // DOWNWARDS
            if (fnDeps.size() > 0) {

                fdf = new FunctionDeclarationFinder();
                for (int jj = 0; jj < fnDeps.size(); jj++) {

                    System.out.println(fnDeps.get(jj).getFunctionName());					

                    fdf.setFunctionArgumentPair(fnDeps.get(jj));

                    boolean declarationAlreadyWatched = false;
                    // If funciton already on watch list, add the argument if it is not watched
                    for (int ty = 0; ty < declarationsToInstrument.size(); ty++) {
                        if (declarationsToInstrument.get(ty).getFunctionName().equals(fnDeps.get(jj).getFunctionName()) ) {
                            declarationAlreadyWatched = true;
                            for (int yw = 0; yw < fnDeps.get(jj).getArgumentsOfInterest().size(); yw++) {
                                if (declarationsToInstrument.get(ty).getArgumentsOfInterest().indexOf(fnDeps.get(jj).getArgumentsOfInterest().get(yw)) == -1) {
                                    declarationsToInstrument.get(ty).addArgumentToWatch(fnDeps.get(jj).getArgumentsOfInterest().get(yw));
                                }
                            }
                        }
                    }
                    // If this is a new function to watch, add it to the list (no prior entry)
                    if (!declarationAlreadyWatched) {
                        declarationsToInstrument.add(fnDeps.get(jj));
                    }

                    ast.visit(fdf);

                    ArrayList<AstNode> asd = fdf.getArgumentsNode();
                    Iterator<AstNode> itt = asd.iterator();
                    AstNode next;

                    while (itt.hasNext()) {
                        next = itt.next();
                        System.out.println("~`~`~`~`~`~`~`~`~`~`~`~`");
                        System.out.println(next.toSource());
                        System.out.println(getDefiningScope(ast, (Name) next).toSource());

                        if (next instanceof Name) {
                            possibleNextSteps.add(new SlicingCriteria(getDefiningScope(ast, (Name) next), ((Name) next).getIdentifier(), false));
                        } 					
                    }
                }
            }
            
            
            
            
            
            
            
            
            
            
            

            // Check if dependencies are new
            it = varDeps.iterator();

            while (it.hasNext()) {
                step = it.next();
                if (step instanceof Name) {
                    possibleNextSteps.add(new SlicingCriteria(getDefiningScope(ast, (Name) step), ((Name) step).getIdentifier(), justFinished.getInter()));
                    
// HERE WAS CONTROL CODE

                    
                } else if (step instanceof KeywordLiteral && step.toSource().equals("this")) {
                    possibleNextSteps.add(new SlicingCriteria(((KeywordLiteral) step).getEnclosingFunction(), "this", justFinished.getInter()));
                } else if (step instanceof PropertyGet) {
                    System.out.println("Property get as dependecy?");
                    System.out.println(step.toSource());
                }
            }
            // Add the new slicing criteria to queue (method checks against existing)
            addToQueue(possibleNextSteps);
        }

        // Actual instrumentation/augmentation of code after all dependencies known
        itsc2 = completedSlices.iterator();
        System.out.println("Completed slicing criteria:");
        System.out.println("===========================");

        SlicingCriteria next;

        while (itsc2.hasNext()) {
            next = itsc2.next();
            System.out.println(next.getVariable() + " ||  " + Token.typeToName(next.getScope().getType()) +  (next.getScope().getType() == org.mozilla.javascript.Token.FUNCTION ? "  | " + ((FunctionNode) next.getScope()).getName() :""));
            System.out.println(next.getScope().toSource());
        }

        // Which ever finish is used, make sure to initialize the global class counter
        ReadWriteReplacer inst = new ReadWriteReplacer();

        // Actual code augmentation/instrumentation happens here, at this point we know all the variables which must be tracked in the file
        /**    while (completedSlices.size() > 0) {			
            justFinished = new SlicingCriteria(completedSlices.get(0).getScope(), completedSlices.get(0).getVariable());

            // Set up parameters for instrumentation once scope if known
            inst.setVariableName(justFinished.getVariable());
            inst.setTopScope(justFinished.getScope());
            // NEW
            scopeOfInterest = justFinished.getScope();

            // Set up parameters for instrumentation once scope if known
            inst.setScopeName(targetFile);
            //wrr.setLineNo(tempLineNo);

            inst.start(new String(input));
            inst.setLineNo(tempLineNo);

            System.out.println("visiting! : " + justFinished.getVariable());
            System.out.println(Token.typeToName(justFinished.getScope().getType()));
            System.out.println("????????????");

            // Start the instrumentation for a single variable
            scopeOfInterest.visit(inst);

            // Tidy up code after all instance of variable have been instrumented
            //ast = ai.finish(ast);

            completedSlices.remove(0);
        }*/

        //inst.setRoot(ast);  see below line
       // System.out.println(ast);
        inst.setVariablesOfInterest(completedSlices, ast);
        System.out.println(scopename);
        inst.setScopeName(scopename);          
        inst.start(new String(input));

        ast.visit(inst);

        //ast = inst.finish(ast);



        // Instrument the function declarations! (track argument data flow)
        FunctionDeclarationInstrumenter fdi = new FunctionDeclarationInstrumenter();
        Iterator<ArgumentPassedIn> apiIt = logTheseArguments.iterator();
        while (apiIt.hasNext()) {	
            ArgumentPassedIn currentDeclarationToIntrument = apiIt.next();

            fdi.setArgumentName(currentDeclarationToIntrument.getArgument());
            fdi.setArgumentNumber(currentDeclarationToIntrument.getArgumentNumber());
            fdi.setTargetFunction(currentDeclarationToIntrument.getFunction());
            fdi.setScopeName(scopename);

            scopeOfInterest = currentDeclarationToIntrument.getFunction().getEnclosingScope();

            fdi.setTopScope(scopeOfInterest);
            fdi.start(new String(input));

            scopeOfInterest.visit(fdi);
        }


        ast = inst.finish(ast);

        System.out.println(ast.toSource());

        /* clean up */
        Context.exit();

        remainingSlices2.addAll(completedSlices);

        completedSlices = new ArrayList<SlicingCriteria>();
        remainingSlices = new ArrayList<SlicingCriteria>();

        return ast.toSource();
    }

    private int isArgument(String varName2, Scope definingScope) throws Exception {
        if (definingScope instanceof FunctionNode) {
            List<AstNode> args = ((FunctionNode) definingScope).getParams();
            Iterator<AstNode> it = args.iterator();
            AstNode nextArg;
            int i = 0;

            while (it.hasNext()) {
                nextArg = it.next();

                if (nextArg instanceof Name) {
                    if (((Name) nextArg).getIdentifier().equals(varName2)) {
                        return i;	
                    }
                } else {
                    System.out.println("[isArgument]: Argument to Function defining scope is not 'Name'");
                    throw new Exception();
                }
                i++;
            }
        }

        // Specified variable was not found function arguments
        return -1;
    }

    private ArrayList<FunctionArgumentPair> fnDeps;

    private ArrayList<AstNode> getDataDependencies (AstRoot ast, SlicingCriteria target) {
        // DEPENDENCY FIND
        DependencyFinder df = new DependencyFinder();

        ArrayList<AstNode> deps = new ArrayList<AstNode>();
        fnDeps = new ArrayList<FunctionArgumentPair>();

        SlicingCriteria newSlice = new SlicingCriteria(target.getScope(), target.getVariable(), true);

        // Set up parameters for instrumentation once scope if known
        df.setVariableName(target.getVariable());
        if (targetFile == null) {
        	System.out.println("Null scope");
        }
        df.setTopScope(target.getScope());			
        df.clearDataDependencies();
        df.setScopeName(targetFile);

        // Start the instrumentation for a single variable
        df.getTopScope().visit(df);		

        // The current slice should be the first in queue, move it to completed queue
        if (remainingSlices.get(0).equals(newSlice)) {
            completedSlices.add(remainingSlices.remove(0));
        } else {
            System.err.println("Error! Insturmentation exeucted out of order!");
        }

        // Get all the related variables to slice iteratively (E.g. LHS/RHS of assignments for initially sliced variable)
        deps =  df.getDataDependencies();
        fnDeps = df.getFunctionsToWatch();

        System.out.println("Size of new vars to slice: " + deps.size());

        return deps;
    }



    private static void addToQueue (ArrayList<SlicingCriteria> addThese) {
        Iterator<SlicingCriteria> itsc;
        SlicingCriteria queuedSlice;

        Iterator<SlicingCriteria> itsc2;
        SlicingCriteria completedSlice;

        Iterator<SlicingCriteria> itnew = addThese.iterator();
        SlicingCriteria newSlice;

        boolean alreadyInQueue;
        boolean alreadyInCompletedQueue;

        while (itnew.hasNext()) {
            newSlice = itnew.next();

            // Initialize iterator for each criteria check
            itsc = remainingSlices.iterator();
            itsc2 = completedSlices.iterator();

            // Check if the recently discovered related variables had already been known
            alreadyInQueue = false;
            alreadyInCompletedQueue = false;

            // Already in queue
            while (itsc.hasNext()) {
                queuedSlice = itsc.next();
                if (queuedSlice.equals(newSlice)) {

                    // Replace old 'downward' only with 'upward and downward'
                    if (newSlice.getInter() == true && queuedSlice.getInter() == false) {
                        remainingSlices.remove(queuedSlice);
                        remainingSlices.add(newSlice);
                        System.out.println("New Name: " + newSlice.getVariable() + " | " + (newSlice.getScope().getType() == org.mozilla.javascript.Token.FUNCTION? ((FunctionNode) newSlice.getScope()).getName(): ""));
                        System.out.println(newSlice.getInter());
                    }
                    alreadyInQueue = true;
                    break;
                }
            }

            // Already instrumented
            while (itsc2.hasNext()) {
                completedSlice = itsc2.next();
                if (completedSlice.equals(newSlice)) {

                    // Re-queue slicing criteria as we are interested in inter function data-flow too (old criteria wasn't)
                    if (newSlice.getInter() == true && completedSlice.getInter() == false) {
                        completedSlices.remove(completedSlice);
                        remainingSlices.add(newSlice);
                        System.out.println("New Name: " + newSlice.getVariable() + " | " + (newSlice.getScope().getType() == org.mozilla.javascript.Token.FUNCTION? ((FunctionNode) newSlice.getScope()).getName(): ""));
                        System.out.println(newSlice.getInter());

                    }

                    alreadyInCompletedQueue = true;
                    break;
                }
            }

            // Debugging:
            if (!alreadyInQueue && !alreadyInCompletedQueue) {
                remainingSlices.add(newSlice);
                System.out.println("New Name: " + newSlice.getVariable() + " | " + (newSlice.getScope().getType() == org.mozilla.javascript.Token.FUNCTION? ((FunctionNode) newSlice.getScope()).getName(): ""));
                System.out.println(newSlice.getInter());

            }
        }
    }

    private Scope getDefiningScope(AstRoot ast, Name target) {
        sc.setScopeName(targetFile);
        sc.setLineNo(target.getLineno());
        sc.setVariableName(target.getIdentifier());

        ast.visit(sc);

        return sc.getLastScopeVisited();
    }
}
