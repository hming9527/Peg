import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hming on 3/16/18.
 */
public class FrontEnd {
    private int numHoles;
    private int numTriples;
    private int initialHole;
    private List<String[]> triples = new ArrayList<>();
    private List<int[]> possibleJumps = new ArrayList<>();
    private List<int[]> holeStates = new ArrayList<>();
    private int numTimepoints;
    private Map<String, List<Integer>> stateActionMap1 = new HashMap<>();
    private Map<String, List<Integer>> stateActionMap2 = new HashMap<>();


    public FrontEnd() {

    }

    public void clausesGenerator(String input) throws IOException {
        File inputFile = new File(input);
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        File outputFile = new File(input + "_Clauses");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] l = line.split("\\s");
            if (l.length == 2) {
                numHoles = Integer.parseInt(l[0]);
                numTimepoints = numHoles - 1;
                initialHole = Integer.parseInt(l[1]);
            } else if (l.length == 3) {
                triples.add(l);
            }
        }
        br.close();
        numTriples = triples.size();
        for (int i = 1; i <= numHoles; i++) {
            for (int j = 1; j <= numTimepoints; j++) {
                holeStates.add(new int[]{i, j});
            }
        }
        for (String[] triple : triples) {
            for (int i = 0; i < triple.length; i += 2) {
                for (int j = 1; j < numTimepoints; j++) {
                    int[] endPoints = new int[]{Integer.parseInt(triple[i]), Integer.parseInt(triple[1]), Integer.parseInt(triple[i == 0 ? 2 : 0]), j};
                    possibleJumps.add(endPoints);
                    String state1 = endPoints[0] + " " + endPoints[3];
                    String state2 = endPoints[1] + " " + endPoints[3];
                    String state3 = endPoints[2] + " " + endPoints[3];
                    int actionIndex = possibleJumps.size();
                    if (!stateActionMap1.containsKey(state1)) stateActionMap1.put(state1, new ArrayList<>());
                    if (!stateActionMap1.containsKey(state2)) stateActionMap1.put(state2, new ArrayList<>());
                    if (!stateActionMap2.containsKey(state3)) stateActionMap2.put(state3, new ArrayList<>());
                    stateActionMap1.get(state1).add(actionIndex);
                    stateActionMap1.get(state2).add(actionIndex);
                    stateActionMap2.get(state3).add(actionIndex);
                }
            }
        }
        List<String> preconditionAxioms = new ArrayList<>();
        List<String> causalAxioms = new ArrayList<>();
        List<String> frameAxioms = new ArrayList<>();
        // Generate precondition axioms, causal axioms and frame axioms
        generateAxioms(preconditionAxioms, causalAxioms, frameAxioms);
        // One action at a time
        List<String> mutexActions = new ArrayList<>();
        for (int i = 1; i < numTimepoints; i++) {
            for (int j = i - 1; j < possibleJumps.size(); j += (numTimepoints - 1)) {
                for (int k = j + numTimepoints - 1; k < possibleJumps.size(); k += (numTimepoints - 1)) {
                    mutexActions.add(-(j + 1) + " " + -(k + 1));
                }
            }
        }
        // Generate starting and ending states
        List<String> initialAndFinalStates = new ArrayList<>();
        String possibleEndings = "";
        for (int i = 1; i <= numHoles; i++) {
            int startIdx = i * numTimepoints + possibleJumps.size() - numTimepoints + 1;
            if (i == initialHole) {
                initialAndFinalStates.add("" + -startIdx);
            } else {
                initialAndFinalStates.add("" + startIdx);
            }
            int endIdx = i * numTimepoints + possibleJumps.size();
            possibleEndings += (endIdx + " ");
            for (int j = i + 1; j <= numHoles; j++) {
                int nextIdx = j * numTimepoints + possibleJumps.size();
                initialAndFinalStates.add(-endIdx + " " + -nextIdx);
            }
        }
        initialAndFinalStates.add(possibleEndings.trim());
        for (String s : preconditionAxioms) {
            bw.write(s);
            bw.newLine();
        }
        for (String s : causalAxioms) {
            bw.write(s);
            bw.newLine();
        }
        for (String s : frameAxioms) {
            bw.write(s);
            bw.newLine();
        }
        for (String s : mutexActions) {
            bw.write(s);
            bw.newLine();
        }
        for (String s : initialAndFinalStates) {
            bw.write(s);
            bw.newLine();
        }
        bw.write("0");
        bw.newLine();
        int i = 1;
        for (; i <= possibleJumps.size(); i++) {
            int[] jump = possibleJumps.get(i - 1);
            String symbol = "Jump(" + jump[0] + "," + jump[1] + "," + jump[2] + "," + jump[3] + ")";
            bw.write(i + " " + symbol);
            bw.newLine();
        }
        for (int j = 1; j <= numHoles; j++) {
            for (int k = 1; k <= numTimepoints; k++) {
                String symbol = "Peg(" + j + "," + k + ")";
                bw.write((i + (j - 1) * numTimepoints + k - 1) + " " + symbol);
                bw.newLine();
            }
        }
        bw.close();
    }

    public void generateAxioms(List<String> preconditionAxioms, List<String> causalAxioms, List<String> frameAxioms) {
        int numJumps = possibleJumps.size();
        for (int i = 1; i <= numJumps; i++) {
            int[] endPoints = possibleJumps.get(i - 1);
            int from = endPoints[0];
            int middle = endPoints[1];
            int to = endPoints[2];
            int timePoint = endPoints[3];
            // Corresponding base state
            int baseFrom = from * numTimepoints + numJumps - numTimepoints;
            int baseMiddle = middle * numTimepoints + numJumps - numTimepoints;
            int baseTo = to * numTimepoints + numJumps - numTimepoints;
            // Generate precondition axioms
            int p1 = baseFrom + timePoint;
            int p2 = baseMiddle + timePoint;
            int p3 = baseTo + timePoint;
            preconditionAxioms.add(-i + " " + p1);
            preconditionAxioms.add(-i + " " + p2);
            preconditionAxioms.add(-i + " " + -p3);
            // Generate causal axioms
            int c1 = baseFrom + timePoint + 1;
            int c2 = baseMiddle + timePoint + 1;
            int c3 = baseTo + timePoint + 1;
            causalAxioms.add(-i + " " + -c1);
            causalAxioms.add(-i + " " + -c2);
            causalAxioms.add(-i + " " + c3);
        }
        generateFrameAxioms(frameAxioms);
    }

    public void generateFrameAxioms(List<String> frameAxioms) {
        for (int i = 1; i <= numHoles; i++) {
            for (int j = 1; j < numTimepoints; j++) {
                String state = i + " " + j;
                System.out.println("State is: " + state);
                int stateIdx = i * numTimepoints + possibleJumps.size() - numTimepoints + j;
                int nextIdx = stateIdx + 1;
                if (stateActionMap1.containsKey(state)) {
                    List<Integer> actionIndices1 = stateActionMap1.get(state);
                    String clause1 = -stateIdx + " " + nextIdx;
                    for (int idx : actionIndices1) {
                        clause1 += (" " + idx);
                    }
                    frameAxioms.add(clause1);
                }
                if (stateActionMap2.containsKey(state)) {
                    List<Integer> actionIndices2 = stateActionMap2.get(state);
                    String clause2 = stateIdx + " " + -nextIdx;
                    for (int idx : actionIndices2) {
                        clause2 += (" " + idx);
                    }
                    frameAxioms.add(clause2);
                }
            }
        }
    }
}
