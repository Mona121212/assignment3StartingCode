package app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Stores a single word and its occurrences across multiple input files.
 * 
 * <p>Each WordRecord tracks:
 * <ul>
 *   <li>The word itself</li>
 *   <li>A mapping of file names to the line numbers where the word appears</li>
 *   <li>A total occurrence count used for the -po report</li>
 * </ul>
 *
 * <p>The class is serializable so it can be saved inside repository.ser.
 */
public class WordRecord implements Comparable<WordRecord>, Serializable {

    private static final long serialVersionUID = 1L;

    /** Map from file name to the list of line numbers where this word appears. */
    private final HashMap<String, ArrayList<Integer>> occurrences;

    /** Total number of times this word appears across all files and all lines. */
    private int occurrenceCount;

    /** The word being stored. */
    private final String word;

    /**
     * Creates a new WordRecord for the specified word.
     *
     * @param word the word to track
     * @throws NullPointerException if the word is null
     */
    public WordRecord(String word) {
        if (word == null)
            throw new NullPointerException("word cannot be null");

        this.word = word;
        this.occurrences = new HashMap<>();
        this.occurrenceCount = 0;
    }

    /**
     * Records that this word appeared on a specific line within a specific file.
     *
     * @param fileName  the name of the file
     * @param lineNumber the line number where the word appeared (must be positive)
     * @throws NullPointerException if fileName is null
     * @throws IllegalArgumentException if lineNumber is &lt;= 0

     */
    public void addLineNumber(String fileName, int lineNumber) {
        if (fileName == null)
            throw new NullPointerException("fileName cannot be null");

        if (lineNumber <= 0)
            throw new IllegalArgumentException("lineNumber must be positive");

        // Increase total occurrence count (counts duplicates for -po)
        occurrenceCount++;

        // Add line number to the file's list (avoiding duplicates)
        ArrayList<Integer> list = occurrences.get(fileName);
        if (list == null) {
            list = new ArrayList<>();
            list.add(lineNumber);
            occurrences.put(fileName, list);
        } else {
            if (!list.contains(lineNumber)) {
                list.add(lineNumber);
            }
        }
    }

    /**
     * Gets the word associated with this record.
     *
     * @return the word being tracked
     */
    public String getWord() {
        return word;
    }

    /**
     * Returns an unmodifiable, sorted copy of the occurrences map.
     *
     * @return a map of file names to sorted unmodifiable lists of line numbers
     */
    public Map<String, List<Integer>> getOccurrences() {
        Map<String, List<Integer>> copy = new HashMap<>();

        for (Map.Entry<String, ArrayList<Integer>> e : occurrences.entrySet()) {
            List<Integer> sorted = new ArrayList<>(e.getValue());
            Collections.sort(sorted);
            copy.put(e.getKey(), Collections.unmodifiableList(sorted));
        }

        return Collections.unmodifiableMap(copy);
    }

    /**
     * Returns the total number of times the word has appeared across all files.
     *
     * @return total occurrence count
     */
    public int getTotalOccurrences() {
        return occurrenceCount;
    }

    /**
     * Compares two WordRecord objects lexicographically (case-insensitive).
     *
     * @param other the WordRecord to compare with
     * @return the comparison result
     * @throws NullPointerException if other is null
     */
    @Override
    public int compareTo(WordRecord other) {
        if (other == null)
            throw new NullPointerException("other cannot be null");

        return this.word.compareToIgnoreCase(other.word);
    }

    /**
     * Produces a string representation of this word and all of its file/line
     * occurrences. Used by the -pl report format.
     *
     * @return a formatted string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(word).append(" -> ");

        StringJoiner filesJoiner = new StringJoiner("; ");
        for (Map.Entry<String, ArrayList<Integer>> e : occurrences.entrySet()) {
            List<Integer> nums = new ArrayList<>(e.getValue());
            Collections.sort(nums);

            StringJoiner numsJoiner = new StringJoiner(", ");
            for (Integer n : nums)
                numsJoiner.add(n.toString());

            filesJoiner.add(e.getKey() + ": [" + numsJoiner.toString() + "]");
        }

        sb.append(filesJoiner.toString());
        return sb.toString();
    }
}
