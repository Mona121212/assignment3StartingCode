// java
package app;


import java.io.*;
import java.util.*;
import java.util.regex.*;

import implementations.BSTree;
import implementations.BSTreeNode;
import utilities.Iterator;

/**
 * Utility application that scans a text file for words, updates a persistent
 * repository of word occurrences (stored in `repository.ser`), and generates
 * reports in multiple formats.
 *
 * <p>The application supports three report types:
 * <ul>
 *   <li>`-pf`: list words followed by the files they appear in</li>
 *   <li>`-pl`: list words with each file and its sorted line numbers</li>
 *   <li>`-po`: list words with each file and its line numbers plus a total count</li>
 * </ul>
 *
 * <p>Usage: java -jar WordTracker.jar &lt;input-file&gt; &lt;-pf|\-pl|\-po&gt; [-f&lt;output.txt&gt;]
 */
public class WordTracker {
    /**
     * File name used to persist the repository (serialized {@link BSTree}).
     */
    private static final String REPO_FILE = "repository.ser";

    /**
     * Main entry point. Validates command-line arguments, loads (or creates) the
     * repository, processes the input file to update word occurrences, generates
     * the requested report (optionally writing to an output file), and saves the
     * repository back to disk.
     *
     * @param args command-line arguments: input file path, report flag (`-pf`, `-pl`, or `-po`),
     *             and optional -f&lt;output-file&gt; to write the report to a file
     */
    public static void main(String[] args) {
    	// Validate command-line arguments
        if (args == null || args.length < 2) {
            System.err.println("Usage: java -jar WordTracker.jar <input-file> <-pf|-pl|-po> [-f<output.txt>]");
            System.exit(1);
        }

        // Parse arguments
        String inputPath = args[0];
        String reportFlag = args[1];

        // Validate report flag
        if (!reportFlag.equals("-pf") && !reportFlag.equals("-pl") && !reportFlag.equals("-po")) {
            System.err.println("Invalid report type. Expected -pf, -pl or -po.");
            System.exit(1);
        }

        // Optional output file
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

        // Process the input file
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

    /**
     * Loads the persisted repository from `repository.ser` if it exists and contains
     * a {@link BSTree} object. If loading fails or the file is missing or has an
     * unexpected contents, a new empty {@link BSTree} is returned.
     *
     * @return the loaded {@link BSTree}&lt;WordRecord&gt; or a new empty tree on failure
     */
    private static BSTree<WordRecord> loadRepository() {
    	// Check if repository file exists
        File f = new File(REPO_FILE);
        if (!f.exists()) {
            // No repository found — return new empty tree
            return new BSTree<>();
        }

        // Attempt to load existing repository
        try (FileInputStream fis = new FileInputStream(f);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

        	// Read object from file
            Object obj = ois.readObject();
            if (obj instanceof BSTree) {
                @SuppressWarnings("unchecked")
                BSTree<WordRecord> tree = (BSTree<WordRecord>) obj;
                return tree;
                // Successfully loaded repository
            } else {
                System.err.println("`" + REPO_FILE + "` does not contain expected BSTree object. Creating new repository.");
                return new BSTree<>();
            }

            // Handle deserialization errors
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load `" + REPO_FILE + "`: " + e.getMessage());
            return new BSTree<>();
        }
    }

    /**
     * Persists the given repository to the `repository.ser` file using Java
     * serialization.
     *
     * @param repository the repository tree to serialize; must not be null
     * @throws IOException if writing to disk fails
     */
    private static void saveRepository(BSTree<WordRecord> repository) throws IOException {
    	// Validate input
        try (FileOutputStream fos = new FileOutputStream(REPO_FILE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(repository);
            oos.flush();
        }
    }

    /**
     * Processes the provided input file, extracts alphabetic words (using Unicode
     * letter detection), and records each occurrence (file name + line number) in
     * the supplied repository tree.
     *
     * <p>Each distinct word is stored as a {@link WordRecord}. If the word is new
     * to the repository it will be inserted; otherwise the existing record is
     * updated with the new occurrence.
     *
     * @param inputFile the text file to scan; must not be null and must exist
     * @param repository the repository to update; must not be null
     * @throws IOException if reading the input file fails
     * @throws NullPointerException if either parameter is null
     */
    public static void processFile(File inputFile, BSTree<WordRecord> repository) throws IOException {
        // Validate inputs
    	if (inputFile == null) {
            throw new NullPointerException("inputFile cannot be null");
        }
    	// Validate repository
        if (repository == null) {
            throw new NullPointerException("repository cannot be null");
        }

        // Regex pattern to match words (Unicode letters)
        Pattern wordPattern = Pattern.compile("\\p{Alpha}+");
        String fileName = inputFile.getName();

        // Read file line by line
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                // Remove apostrophes so contractions like "it's" become "its" (not "it" + "s")
                line = line.replace("'", "");
                
                
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


    /**
     * Generates a report from the repository according to the selected report
     * flag. The report is printed to standard output and optionally duplicated to
     * a specified file.
     *
     * <p>Report formats:
     * <ul>
     *   <li>`-pf`: word -> file1; file2</li>
     *   <li>`-pl`: uses {@link WordRecord#toString()} which shows files with sorted line numbers</li>
     *   <li>`-po`: word -> file1: [lines] ; Total: N</li>
     * </ul>
     *
     * @param repository the repository to iterate for report generation; may be empty but not null
     * @param reportFlag one of `-pf`, `-pl`, or `-po` indicating the report format
     * @param outputFile optional file path to also write the report to; if null, report is only written to stdout
     * @throws IOException if opening or writing to the optional output file fails
     */
    private static void generateReport(BSTree<WordRecord> repository, String reportFlag, String outputFile) throws IOException {
        // Validate input
    	PrintWriter pw = null;
        PrintWriter console = new PrintWriter(System.out, true);
        boolean closeWriter = false;
        
        // Determine output destination
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

            // Iterate through repository and generate report lines
            Iterator<WordRecord> it = repository.inorderIterator();
            while (it.hasNext()) {
                WordRecord wr = it.next();
                if (wr == null) continue;
                Map<String, List<Integer>> occ = wr.getOccurrences();

                // Prepare report line based on selected format
                String line;
                if (reportFlag.equals("-pf")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(wr.getWord()).append(" -> ");
                    boolean first = true;
                    // Append file names only
                    for (String fname : occ.keySet()) {
                        if (!first) sb.append("; ");
                        sb.append(fname);
                        first = false;
                    }
                    line = sb.toString();
                    // word -> file1; file2
                } else if (reportFlag.equals("-pl")) {
                    line = wr.toString();
                    // uses WordRecord.toString()
                } else { // -po
                	int total = wr.getTotalOccurrences();
                	StringBuilder sb = new StringBuilder();
                	sb.append(wr.getWord()).append(" -> ");
                	boolean firstFile = true;
                	for (Map.Entry<String, List<Integer>> e : occ.entrySet()) {
                	    if (!firstFile) sb.append("; ");
                	    sb.append(e.getKey()).append(": ").append(e.getValue().toString());
                	    firstFile = false;
                	}
                	sb.append(" ; Total: ").append(total);
                	line = sb.toString();
                }

                // Print the report line
                pw.println(line);
                pw.flush();
                if (outputFile != null) {
                    console.println(line);
                    console.flush();
                }
            }
            // Finished report generation
        } finally {
            if (closeWriter && pw != null) {
                pw.close();
            }
        }
    }

}
