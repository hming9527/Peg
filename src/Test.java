import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hming on 3/15/18.
 */
public class Test {

    public static void main(String[] args) throws IOException {
        DPLL dpll = new DPLL();
        FrontEnd frontEnd = new FrontEnd();
        BackEnd backEnd = new BackEnd();
        String puzzleInput = "/Users/hming/Documents/AI/Peg/src/10_hole_input";
//        frontEnd.clausesGenerator(puzzleInput);
        String puzzleOutput = puzzleInput + "_Clauses";
//        dpll.initialize(puzzleOutput);
        String dpllOutput = puzzleOutput + "_DPLL_output";
        backEnd.generatePath(dpllOutput);
    }
}
