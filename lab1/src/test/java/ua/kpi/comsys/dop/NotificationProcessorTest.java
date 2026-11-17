package ua.kpi.comsys.dop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationProcessorTest {

    private NotificationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new NotificationProcessor();
    }

    @Test
    void processTextMessage() {
        String expected = """
                Notification: TEXT
                From: alice
                Message: Hello, Bob!
                """;

        assertEquals(expected, processor.process(new TextMsg("alice", "Hello, Bob!")));
    }

    @Test
    void processImageMessage() {
        String expected = """
                Notification: IMAGE
                From: bob
                URL: https://example.com/cat.png (512 KB)
                """;

        assertEquals(expected, processor.process(new ImageMsg("bob", "https://example.com/cat.png", 512)));
    }

    @Test
    void processAudioMessage() {
        String expected = """
                Notification: AUDIO
                From: carol
                Duration: 42 seconds
                """;

        assertEquals(expected, processor.process(new AudioMsg("carol", 42)));
    }

    @Test
    void processNullThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> processor.process(null));
        assertEquals("Notification cannot be null", e.getMessage());
    }
}
