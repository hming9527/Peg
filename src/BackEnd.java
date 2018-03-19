import java.io.*;
import java.util.*;

/**
 * Created by hming on 3/18/18.
 */
public class BackEnd {

    private Map<String, Boolean> values = new HashMap<>();
    private Map<String, String> symbolTable = new HashMap<>();

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
        while ((line = br.readLine()) != null) {
            if (lineIdx == 0 && line.equals("0")) {
                bw.write("NO SOLUTION");
                bw.close();
                return;
            } else {
                if (line.equals("0")) {
                    isValue = false;
                    lineIdx++;
                } else {
                    String[] arr = line.split("\\s");
                    if (arr.length < 2) continue;
                    if (isValue) {
                        values.put(arr[0], arr[1].equals("T") ? true : false);
                    } else {
                        symbolTable.put(arr[0], arr[1]);
                    }
                    lineIdx++;
                }
            }
        }
        br.close();
        List<String> path = new ArrayList<>();
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
        Collections.sort(path, myComparator);
        for (String s : path) {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }
}
