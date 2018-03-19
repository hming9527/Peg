import java.io.IOException;

/**
 * Peg solver.
 * The peg solver takes as argument an input file path, and generates the path that solves the peg game.
 */
public class PegSolver {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java PegSolver path_to_input_puzzle");
            System.exit(-1);
        }
        DPLL dpll = new DPLL();
        FrontEnd frontEnd = new FrontEnd();
        BackEnd backEnd = new BackEnd();
        String puzzleInput = args[0];
        frontEnd.clausesGenerator(puzzleInput);
        String puzzleOutput = puzzleInput + "_Clauses";
        dpll.dp(puzzleOutput);
        String dpllOutput = puzzleOutput + "_DPLL_output";
        backEnd.generatePath(dpllOutput);
    }
}
