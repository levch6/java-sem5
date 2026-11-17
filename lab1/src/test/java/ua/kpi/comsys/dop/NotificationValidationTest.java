package ua.kpi.comsys.dop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationValidationTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void textMsgRejectsInvalidSender(String sender) {
        assertThrows(IllegalArgumentException.class, () -> new TextMsg(sender, "text"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void textMsgRejectsInvalidText(String text) {
        assertThrows(IllegalArgumentException.class, () -> new TextMsg("alice", text));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void imageMsgRejectsInvalidSender(String sender) {
        assertThrows(IllegalArgumentException.class, () -> new ImageMsg(sender, "https://example.com/a.png", 10));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void imageMsgRejectsInvalidUrl(String url) {
        assertThrows(IllegalArgumentException.class, () -> new ImageMsg("bob", url, 10));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void imageMsgRejectsNonPositiveSize(int sizeKb) {
        assertThrows(IllegalArgumentException.class, () -> new ImageMsg("bob", "https://example.com/a.png", sizeKb));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void audioMsgRejectsInvalidSender(String sender) {
        assertThrows(IllegalArgumentException.class, () -> new AudioMsg(sender, 5));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void audioMsgRejectsNonPositiveDuration(int duration) {
        assertThrows(IllegalArgumentException.class, () -> new AudioMsg("carol", duration));
    }

    @Test
    void validRecordsAreCreated() {
        assertDoesNotThrow(() -> new TextMsg("a", "b"));
        assertDoesNotThrow(() -> new ImageMsg("a", "b", 1));
        assertDoesNotThrow(() -> new AudioMsg("a", 1));
    }
}
