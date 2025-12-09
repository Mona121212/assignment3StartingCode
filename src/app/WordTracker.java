// java
package app;


import java.io.*;
import java.util.*;
import java.util.regex.*;

import implementations.BSTree;
import implementations.BSTreeNode;
import utilities.Iterator;
import app.WordRecord;

public class WordTracker {
    private static final String REPO_FILE = "repository.ser";

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Usage: java -jar WordTracker.jar <input-file> <-pf|-pl|-po> [-f<output.txt>]");
            System.exit(1);
        }

        String inputPath = args[0];
        String reportFlag = args[1];

        if (!reportFlag.equals("-pf") && !reportFlag.equals("-pl") && !reportFlag.equals("-po")) {
            System.err.println("Invalid report type. Expected -pf, -pl or -po.");
            System.exit(1);
        }

        String outputFile = null;
        if (args.length >= 3) {
            String arg2 = args[2];
            if (arg2.startsWith("-f") && arg2.length() > 2) {
                outputFile = arg2.substring(2);
            } else {
                System.err.println("Invalid output flag. Expected -f<filename>");
                System.exit(1);
            }
        }

        // Load existing repository or create a new one
        BSTree<WordRecord> repository = loadRepository();

        // Process the input file and update repository
        File inputFile = new File(inputPath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            System.err.println("Input file not found: " + inputPath);
            System.exit(1);
        }

        try {
            processFile(inputFile, repository);
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            System.exit(1);
        }

        // Generate the report (writes to System.out or specified file)
        try {
            generateReport(repository, reportFlag, outputFile);
        } catch (IOException e) {
            System.err.println("Failed to write report: " + e.getMessage());
            System.exit(1);
        }

        // Persist repository to `repository.ser`
        try {
            saveRepository(repository);
        } catch (IOException e) {
            System.err.println("Failed to save repository: " + e.getMessage());
            System.exit(1);
        }

        // Basic confirmation
        System.out.println("Repository size: " + repository.size());
    }

    private static BSTree<WordRecord> loadRepository() {
        File f = new File(REPO_FILE);
        if (!f.exists()) {
            // No repository found — return new empty tree
            return new BSTree<>();
        }

        try (FileInputStream fis = new FileInputStream(f);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            Object obj = ois.readObject();
            if (obj instanceof BSTree) {
                @SuppressWarnings("unchecked")
                BSTree<WordRecord> tree = (BSTree<WordRecord>) obj;
                return tree;
            } else {
                System.err.println("`" + REPO_FILE + "` does not contain expected BSTree object. Creating new repository.");
                return new BSTree<>();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load `" + REPO_FILE + "`: " + e.getMessage());
            return new BSTree<>();
        }
    }

    private static void saveRepository(BSTree<WordRecord> repository) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(REPO_FILE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(repository);
            oos.flush();
        }
    }

    public static void processFile(File inputFile, BSTree<WordRecord> repository) throws IOException {
        if (inputFile == null) {
            throw new NullPointerException("inputFile cannot be null");
        }
        if (repository == null) {
            throw new NullPointerException("repository cannot be null");
        }

        Pattern wordPattern = Pattern.compile("\\p{Alpha}+");
        String fileName = inputFile.getName();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                Matcher m = wordPattern.matcher(line);
                while (m.find()) {
                    String token = m.group().toLowerCase();

                    // search tree for existing WordRecord
                    WordRecord key = new WordRecord(token);
                    BSTreeNode<WordRecord> node = repository.search(key);

                    if (node == null) {
                        // new word -> create record, add location, insert into tree
                        WordRecord wr = new WordRecord(token);
                        wr.addLineNumber(fileName, lineNum);
                        repository.add(wr);
                    } else {
                        // existing word -> update existing record
                        node.getElement().addLineNumber(fileName, lineNum);
                    }
                }
            }
        }
    }


    private static void generateReport(BSTree<WordRecord> repository, String reportFlag, String outputFile) throws IOException {
        PrintWriter pw = null;
        PrintWriter console = new PrintWriter(System.out, true);
        boolean closeWriter = false;
        try {
            if (outputFile != null) {
                pw = new PrintWriter(new FileWriter(outputFile), true);
                closeWriter = true;
            } else {
                pw = console;
                closeWriter = false;
            }

            // Prepare and print a short legend/header explaining the chosen report format
            String legend;
            if (reportFlag.equals("-pf")) {
                legend = "Legend: `-pf`  -> word followed by the files it appears in (file names). Example: word -> file1.txt; file2.txt";
            } else if (reportFlag.equals("-pl")) {
                legend = "Legend: `-pl`  -> words listed with each file and its sorted line numbers. Example: word -> file1.txt: [1, 3]; file2.txt: [2]";
            } else { // -po
                legend = "Legend:`-po' -> words listed with each file and its line numbers plus a Total count of occurrences across all files. Example: word -> file1.txt: [1] ; Total: 1";
            }

            pw.println(legend);
            pw.println(); // blank line after legend
            pw.flush();
            if (outputFile != null) {
                console.println(legend);
                console.println();
                console.flush();
            }

            Iterator<WordRecord> it = repository.inorderIterator();
            while (it.hasNext()) {
                WordRecord wr = it.next();
                if (wr == null) continue;
                Map<String, List<Integer>> occ = wr.getOccurrences();

                String line;
                if (reportFlag.equals("-pf")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(wr.getWord()).append(" -> ");
                    boolean first = true;
                    for (String fname : occ.keySet()) {
                        if (!first) sb.append("; ");
                        sb.append(fname);
                        first = false;
                    }
                    line = sb.toString();
                } else if (reportFlag.equals("-pl")) {
                    line = wr.toString();
                } else { // -po
                    int total = 0;
                    StringBuilder sb = new StringBuilder();
                    sb.append(wr.getWord()).append(" -> ");
                    boolean firstFile = true;
                    for (Map.Entry<String, List<Integer>> e : occ.entrySet()) {
                        if (!firstFile) sb.append("; ");
                        sb.append(e.getKey()).append(": ").append(e.getValue().toString());
                        total += e.getValue().size();
                        firstFile = false;
                    }
                    sb.append(" ; Total: ").append(total);
                    line = sb.toString();
                }

                pw.println(line);
                pw.flush();
                if (outputFile != null) {
                    console.println(line);
                    console.flush();
                }
            }
        } finally {
            if (closeWriter && pw != null) {
                pw.close();
            }
        }
    }

}
