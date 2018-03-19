import java.io.*;
import java.util.*;

/**
 * Created by hming on 3/9/18.
 */
public class DPLL {
    private Set<String> atoms = new HashSet<>();
    private List<String> atomList = new ArrayList<>();
    private Set<String> literals = new HashSet<>();
    private LinkedList<Set<String>> clauses = new LinkedList<>();
    private Map<String, Integer> assignments = new HashMap<>();

    public DPLL() {

    }

    public void initialize(String input) throws IOException {
        File inputFile = new File(input);
        BufferedReader bf = new BufferedReader(new FileReader(inputFile));
        File outputFile = new File(input + "_DPLL_output");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line = "";
        String reproduction = "";
        boolean isClause = true;
        while ((line = bf.readLine()) != null) {
            if (line.equals("0")) isClause = false;
            if (isClause) {
                String[] clause = line.split("\\s");
                Set<String> s = new HashSet<>();
                for (String c : clause) {
                    if (c.length() == 0) continue;
                    s.add(c);
                    literals.add(c);
                    if (c.charAt(0) == '-') {
                        atoms.add(c.substring(1));
                        assignments.put(c.substring(1), -1);
                    } else {
                        atoms.add(c);
                        assignments.put(c, -1);
                    }
                }
                clauses.add(s);
            } else {
                reproduction += (line + "\n");
            }
        }
        atomList = new ArrayList<>(atoms);
        Collections.sort(atomList);
        System.out.println("check atoms set");
        for (String a : atoms) {
            System.out.println(a);
        }
        System.out.println("check literals set");
        for (String l : literals) {
            System.out.println(l);
        }
        System.out.println("check clauses");
        for (Set<String> s : clauses) {
            for (String ss : s) {
                System.out.print(ss + " ");
            }
            System.out.println();
        }
        boolean satisfied = false;
        satisfied = dp1(clauses, assignments);
        if (satisfied) {
            System.out.println("Satisfied");
            System.out.println("assignments");
            for (String k : assignments.keySet()) {
                System.out.println(k + " " + assignments.get(k));
            }
            List<String> atomsList = new ArrayList<>(atoms);
            Collections.sort(atomsList, Comparator.comparingInt((String s) -> Integer.parseInt(s)));
            for (String a : atomsList) {
                String val = assignments.get(a) == 1 ? "T" : "F";
                bw.write(a + " " + val);
                bw.newLine();
            }
        }
        bw.write(reproduction);
        bw.close();
    }

    public boolean dp1(LinkedList<Set<String>> clauseSet, Map<String, Integer> atomValues) {
        while (true) {
            // Success: All clauses are satisfied, unbounded atoms are assigned to be true by default.
            if (clauseSet.isEmpty()) {
                for (String atom : atoms) {
                    if (atomValues.get(atom) == -1) {
                        atomValues.put(atom, 1);
                    }
                }
                System.out.println("Success");
                System.out.println("assignments");
                for (String k : atomValues.keySet()) {
                    System.out.println(k + " " + atomValues.get(k));
                }
                assignments = atomValues;
                return true;
            }

            // Failure: Some clause is unsatisfiable.
            for (Set<String> clause : clauseSet) {
                if (clause.isEmpty()) {
                    System.out.println("empty clause, fail");
                    return false;
                }
            }

            // Pure literal elimination.
            boolean literalElimination = false;
            Set<String> curLiterals = new HashSet<>();
            for (Set<String> clause : clauseSet) {
                for (String l : clause) curLiterals.add(l);
            }
            literals = curLiterals;
            for (String l : literals) {
                String negation = l.charAt(0) == '-' ? l.substring(1) : ("-" + l);
                if ((atomValues.containsKey(l) && atomValues.get(l) != -1) || (atomValues.containsKey(negation) && atomValues.get(negation) != -1)) continue;
                if (!literals.contains(negation)) {
                    System.out.println("Pure literal elimination, " + l);
                    literalElimination = true;
                    obviousAssign(l, atomValues);
                    Iterator<Set<String>> iter = clauseSet.iterator();
                    while (iter.hasNext()) {
                        Set<String> clause = iter.next();
                        if (clause.contains(l)) {
                            iter.remove();
                        }
                    }
                    System.out.println("Clause set after pure literal elimination");
                    for (Set<String> s : clauseSet) {
                        for (String ss : s) {
                            System.out.print(ss + " ");
                        }
                        System.out.println();
                    }
                    System.out.println("assignments");
                    for (String k : atomValues.keySet()) {
                        System.out.println(k + " " + atomValues.get(k));
                    }
                    break;
                }
            }
            if (literalElimination) continue;

            // Forced assignment.
            boolean forcedAssignment = false;
            String l = "";
            for (Set<String> clause : clauseSet) {
                if (clause.size() == 1) {
                    System.out.println("Single literal");
                    for (String c : clause) {
                        System.out.println(c);
                    }
                    List<String> temp = new ArrayList<>(clause);
                    l = temp.get(0);
                    System.out.println("Forced assignment, " + l);
                    forcedAssignment = true;
                    break;
                }
            }
            if (forcedAssignment) {
                obviousAssign(l, atomValues);
                propagate(l, clauseSet, atomValues);
                continue;
            }

            // No easy cases found, exit loop.
            System.out.println("No easy case found.");
            break;
        }
        // Hard case: Pick some atom and try each assignment in turn.
        String pick = "";
        for (String a : atomList) {
            if (atomValues.get(a) == -1) {
                pick = a;
                break;
            }
        }
        if (pick.equals("")) return true;
        // Try to assign value true to the picked atom.
        System.out.println("Picked atom: " + pick);
        atomValues.put(pick, 1);
        System.out.println(pick + " is true");
        LinkedList<Set<String>> clausesCopy = new LinkedList<>();
        Map<String, Integer> atomValuesCopy = new HashMap<>(atomValues);
        for (Set<String> clause : clauseSet) {
            Set<String> clauseCopy = new HashSet<>(clause);
            clausesCopy.add(clauseCopy);
        }
        propagate(pick, clausesCopy, atomValuesCopy);
        boolean firstTry = dp1(clausesCopy, atomValuesCopy);
        if (firstTry) {
            clauseSet = clausesCopy;
            atomValues = atomValuesCopy;
            return firstTry;
        }
        // Try to assign value false to the picked atom.
        System.out.println(pick + " is false");
        atomValues.put(pick, 0);
        propagate(pick, clauseSet, atomValues);
        boolean secondTry =  dp1(clauseSet, atomValues);
        return secondTry;
    }

    public void propagate(String l, LinkedList<Set<String>> s, Map<String, Integer> m) {
        String a = l.charAt(0) == '-' ? l.substring(1) : l;
        String na = "-" + a;
        Iterator<Set<String>> iter = s.iterator();
        while (iter.hasNext()) {
            Set<String> clause = iter.next();
            if ((clause.contains(a) && m.get(a) == 1) || (clause.contains(na) && m.get(a) == 0)) {
                iter.remove();
            } else if (clause.contains(a) && m.get(a) == 0) {
                clause.remove(a);
            } else if (clause.contains(na) && m.get(a) == 1) {
                clause.remove(na);
            }
        }
        System.out.println("Call propagate");
        System.out.println("new clauses");
        for (Set<String> c : s) {
            for (String ss : c) {
                System.out.print(ss + " ");
            }
            System.out.println();
        }
        System.out.println("assignments");
        for (String k : m.keySet()) {
            System.out.println(k + " " + m.get(k));
        }
    }

    public void obviousAssign(String l, Map<String, Integer> m) {
        if (l.charAt(0) == '-') {
            m.put(l.substring(1), 0);
        } else {
            m.put(l, 1);
        }
    }
}
