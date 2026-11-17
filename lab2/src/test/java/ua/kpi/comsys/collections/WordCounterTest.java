package ua.kpi.comsys.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordCounterTest {

    private WordCounter counter;

    @BeforeEach
    void setUp() {
        counter = new WordCounter();
    }

    @Test
    void countWordsCountsOccurrencesAcrossLines() {
        List<String> lines = List.of("the quick brown fox", "the lazy dog", "The end");

        Map<String, Integer> result = counter.countWords(lines);

        assertEquals(Map.of(
                "the", 3,
                "quick", 1,
                "brown", 1,
                "fox", 1,
                "lazy", 1,
                "dog", 1,
                "end", 1
        ), result);
    }

    @Test
    void countWordsIgnoresCase() {
        Map<String, Integer> result = counter.countWords(List.of("Java JAVA java JaVa"));

        assertEquals(Map.of("java", 4), result);
    }

    @Test
    void countWordsSplitsOnPunctuationAndWhitespace() {
        Map<String, Integer> result = counter.countWords(List.of("Hello, world!  Hello...world?\tagain"));

        assertEquals(Map.of("hello", 2, "world", 2, "again", 1), result);
    }

    @Test
    void countWordsKeepsDigitsAsPartOfWords() {
        Map<String, Integer> result = counter.countWords(List.of("java21 and java21, 42 times"));

        assertEquals(Map.of("java21", 2, "and", 1, "42", 1, "times", 1), result);
    }

    @Test
    void countWordsTreatsNonLatinCharactersAsSeparators() {
        Map<String, Integer> result = counter.countWords(List.of("abc-привіт-def"));

        assertEquals(Map.of("abc", 1, "def", 1), result);
    }

    @Test
    void countWordsReturnsEmptyMapForEmptyInput() {
        assertTrue(counter.countWords(List.of()).isEmpty());
        assertTrue(counter.countWords(List.of("", "   ", "!!! ,,,")).isEmpty());
    }

    @Test
    void countWordsSkipsNullLines() {
        Map<String, Integer> result = counter.countWords(Arrays.asList("one", null, "one two"));

        assertEquals(Map.of("one", 2, "two", 1), result);
    }

    @Test
    void countWordsThrowsOnNullInput() {
        assertThrows(IllegalArgumentException.class, () -> counter.countWords(null));
    }

    @Test
    void getUniqueWordsReturnsDistinctLowercaseWords() {
        Set<String> result = counter.getUniqueWords(List.of("To be, or NOT to be", "that is the question"));

        assertEquals(Set.of("to", "be", "or", "not", "that", "is", "the", "question"), result);
    }

    @Test
    void getUniqueWordsReturnsEmptySetForEmptyInput() {
        assertTrue(counter.getUniqueWords(List.of()).isEmpty());
        assertTrue(counter.getUniqueWords(List.of(" ", "?!")).isEmpty());
    }

    @Test
    void getUniqueWordsSkipsNullLines() {
        Set<String> result = counter.getUniqueWords(Arrays.asList(null, "Alpha beta", null));

        assertEquals(Set.of("alpha", "beta"), result);
    }

    @Test
    void getUniqueWordsThrowsOnNullInput() {
        assertThrows(IllegalArgumentException.class, () -> counter.getUniqueWords(null));
    }

    @Test
    void uniqueWordsMatchCountedKeys() {
        List<String> lines = List.of("a b c a", "B C d");

        assertEquals(counter.countWords(lines).keySet(), counter.getUniqueWords(lines));
    }
}
