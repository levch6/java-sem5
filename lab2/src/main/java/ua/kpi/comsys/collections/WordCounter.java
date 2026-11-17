package ua.kpi.comsys.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class WordCounter {

    private static final Pattern WORD_SEPARATOR = Pattern.compile("[^A-Za-z0-9]+");

    public Map<String, Integer> countWords(List<String> textLines) {
        validate(textLines);

        Map<String, Integer> counts = new HashMap<>();
        for (String word : extractWords(textLines)) {
            counts.merge(word, 1, Integer::sum);
        }
        return counts;
    }

    public Set<String> getUniqueWords(List<String> textLines) {
        validate(textLines);

        return new HashSet<>(extractWords(textLines));
    }

    private static void validate(List<String> textLines) {
        if (textLines == null) {
            throw new IllegalArgumentException("textLines cannot be null");
        }
    }

    private static List<String> extractWords(List<String> textLines) {
        List<String> words = new ArrayList<>();
        for (String line : textLines) {
            if (line == null) {
                continue;
            }
            for (String token : WORD_SEPARATOR.split(line)) {
                if (!token.isEmpty()) {
                    words.add(token.toLowerCase(Locale.ROOT));
                }
            }
        }
        return words;
    }
}
