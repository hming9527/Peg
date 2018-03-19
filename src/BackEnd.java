import java.io.*;
import java.util.*;

/**
 * Backend part of the peg solver.
 * The backend takes as input the the output of the DPLL program, and generates the path represented by the output.
 */
public class BackEnd {

    private Map<String, Boolean> values = new HashMap<>();
    private Map<String, String> symbolTable = new HashMap<>();
    private int expectedNumSteps = -1;

    public BackEnd() {

    }

    public void generatePath(String input) throws IOException {
        File inputFile = new File(input);
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String outputFile = input + "_Final_Path";
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line = "";
        int lineIdx = 0;
        boolean isValue = true;
        boolean numStepsConfirmed = false;
        while ((line = br.readLine()) != null) {
            // If the input indicates suggests that the peg game has no solution,
            // the backend writes "NO SOLUTION" to the output and return.
            if (lineIdx == 0 && line.equals("0")) {
                bw.write("NO SOLUTION");
                bw.close();
                return;
            } else {
                if (line.equals("0")) {
                    isValue = false;
                    lineIdx++;
                } else {
                    // Construct value map and symbol table.
                    String[] arr = line.split("\\s");
                    if (arr.length < 2) continue;
                    if (isValue) {
                        values.put(arr[0], arr[1].equals("T") ? true : false);
                    } else {
                        symbolTable.put(arr[0], arr[1]);
                        if (!numStepsConfirmed && arr[1].startsWith("P")) {
                            String[] states = arr[1].split(",");
                            int timepoint = Integer.parseInt(states[states.length - 1].substring(0, states[states.length - 1].length() - 1));
                            if (timepoint - 1 > expectedNumSteps) {
                                expectedNumSteps = timepoint - 1;
                            } else {
                                numStepsConfirmed = true;
                            }
                        }
                    }
                    lineIdx++;
                }
            }
        }
        br.close();
        List<String> path = new ArrayList<>();
        // Add selected jump actions to the path.
        for (String k : values.keySet()) {
            if (values.get(k) && symbolTable.get(k).startsWith("J")) {
                path.add(symbolTable.get(k));
            }
        }
        Comparator<String> myComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] arr1 = o1.split(",");
                String[] arr2 = o2.split(",");
                String time1 = arr1[arr1.length - 1];
                String time2 = arr2[arr2.length - 1];
                time1 = time1.substring(0, time1.length() - 1);
                time2 = time2.substring(0, time2.length() - 1);
                return Integer.parseInt(time1) - Integer.parseInt(time2);
            }
        };
        // Sort the path by the timepoint of each jump.
        Collections.sort(path, myComparator);
        // Write the path to the output.
        // If the path generated can not be a real path, write "NO SOLUTION" and return.
        if (path.size() == 0 || path.size() != expectedNumSteps) {
            bw.write("NO SOLUTION");
            return;
        }
        for (String s : path) {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }
}
