// java
package app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import implementations.BSTree;



/**
 * Stores a word and its occurrences (file -> list of line numbers).
 */
public class WordRecord implements Comparable<WordRecord>, Serializable {
    private static final long serialVersionUID = 1L;

    private final String word;
    private final HashMap<String, ArrayList<Integer>> occurrences;

    public WordRecord(String word) {
        if (word == null) {
            throw new NullPointerException("word cannot be null");
        }
        this.word = word;
        this.occurrences = new HashMap<>();
    }

    /**
     * Add a line number for a given file. Duplicates for the same file/line are ignored.
     */
    public void addLineNumber(String fileName, int lineNumber) {
        if (fileName == null) {
            throw new NullPointerException("fileName cannot be null");
        }
        if (lineNumber <= 0) {
            throw new IllegalArgumentException("lineNumber must be positive");
        }
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
     * Returns the stored word.
     */
    public String getWord() {
        return word;
    }

    /**
     * Returns an unmodifiable view of occurrences map.
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
     * Compare by word (case-insensitive natural ordering).
     */
    @Override
    public int compareTo(WordRecord other) {
        if (other == null) {
            throw new NullPointerException("other cannot be null");
        }
        return this.word.compareToIgnoreCase(other.word);
    }

    /**
     * Produces a readable representation for reports, e.g.:
     * word -> file1: [1, 3, 7]; file2: [2, 4]
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(word).append(" -> ");
        StringJoiner filesJoiner = new StringJoiner("; ");
        for (Map.Entry<String, ArrayList<Integer>> e : occurrences.entrySet()) {
            List<Integer> nums = new ArrayList<>(e.getValue());
            Collections.sort(nums);
            StringJoiner numJoiner = new StringJoiner(", ");
            for (Integer n : nums) {
                numJoiner.add(n.toString());
            }
            filesJoiner.add(e.getKey() + ": [" + numJoiner.toString() + "]");
        }
        sb.append(filesJoiner.toString());
        return sb.toString();
    }
}
