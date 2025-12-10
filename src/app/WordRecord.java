// java
package app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Stores a single word and its occurrences across files.
 *
 * <p>Occurrences are tracked as a map from file name to a list of line numbers.
 * Lists returned by {@link #getOccurrences()} are sorted and unmodifiable; the
 * internal representation preserves insertion and avoids exposing mutable lists.
 */
public class WordRecord implements Comparable<WordRecord>, Serializable {
	
	// Map from file name to list of line numbers
	private final HashMap<String, ArrayList<Integer>> occurrences;

	// Total number of times this word appears (all files, all lines)
	private int occurrenceCount;

    
	// Serial version UID for serialization
	private static final long serialVersionUID = 1L;

    // The word being recorded
    private final String word;

    // Constructor
    public WordRecord(String word) {
    	// Validate input
        if (word == null) {
            throw new NullPointerException("word cannot be null");
        }
        this.word = word;
        this.occurrences = new HashMap<>();
        this.occurrenceCount = 0;
    }

    // Add a line number occurrence for a given file
    public void addLineNumber(String fileName, int lineNumber) {
    	// Validate inputs
        if (fileName == null) {
            throw new NullPointerException("fileName cannot be null");
        }
        // Validate line number
        if (lineNumber <= 0) {
            throw new IllegalArgumentException("lineNumber must be positive");
        }
        
        // Always increment total occurrences (even if same line as before)
        occurrenceCount++;
        
        // Get or create the list of line numbers for the file
        ArrayList<Integer> list = occurrences.get(fileName);
        if (list == null) {
            list = new ArrayList<>();
            list.add(lineNumber);
            occurrences.put(fileName, list);
            // First occurrence for this file
        } else {
            if (!list.contains(lineNumber)) {
                list.add(lineNumber);
            }
        }
    }

    // Get the recorded word
    public String getWord() {
        return word;
    }

    // Get a copy of the occurrences map with sorted, unmodifiable lists
    public Map<String, List<Integer>> getOccurrences() {
        Map<String, List<Integer>> copy = new HashMap<>();
        // Create sorted, unmodifiable copies of the line number lists
        for (Map.Entry<String, ArrayList<Integer>> e : occurrences.entrySet()) {
            List<Integer> sorted = new ArrayList<>(e.getValue());
            Collections.sort(sorted);
            copy.put(e.getKey(), Collections.unmodifiableList(sorted));
        }
        return Collections.unmodifiableMap(copy);
    }

    // Compare WordRecords based on the word, case-insensitively
    @Override
    public int compareTo(WordRecord other) {
        if (other == null) {
            throw new NullPointerException("other cannot be null");
        }
        return this.word.compareToIgnoreCase(other.word);
    }
    
    public int getTotalOccurrences() {
        return occurrenceCount;
    }

    // String representation of the WordRecord
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(word).append(" -> ");
        StringJoiner filesJoiner = new StringJoiner("; ");
        // Sort file names for consistent output
        for (Map.Entry<String, ArrayList<Integer>> e : occurrences.entrySet()) {
            List<Integer> nums = new ArrayList<>(e.getValue());
            Collections.sort(nums);
            StringJoiner numJoiner = new StringJoiner(", ");
            // Add sorted line numbers
            for (Integer n : nums) {
                numJoiner.add(n.toString());
            }
            filesJoiner.add(e.getKey() + ": [" + numJoiner.toString() + "]");
        }
        sb.append(filesJoiner.toString());
        return sb.toString();
    }
}
