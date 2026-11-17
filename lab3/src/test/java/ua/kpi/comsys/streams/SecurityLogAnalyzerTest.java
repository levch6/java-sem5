package ua.kpi.comsys.streams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(2)
class SecurityLogAnalyzerTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 0, 0);

    private SecurityLogAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SecurityLogAnalyzer();
    }

    private static SecurityLog log(String ip, String severity, int minute) {
        return new SecurityLog(ip, severity, "event " + minute, START.plusMinutes(minute));
    }

    private static Stream<SecurityLog> infinite(IntFunction<SecurityLog> generator) {
        return Stream.iterate(0, i -> i + 1).map(generator::apply);
    }

    @Nested
    class FindFirstCriticalEvent {

        @Test
        void findsCriticalEventInInfiniteStream() {
            Stream<SecurityLog> stream = infinite(i -> log("10.0.0." + (i % 5), i == 1000 ? "CRITICAL" : "INFO", i));

            Optional<SecurityLog> result = analyzer.findFirstCriticalEvent(stream);

            assertEquals(Optional.of("event 1000"), result.map(SecurityLog::eventDescription));
        }

        @Test
        void returnsFirstOfSeveralCriticalEvents() {
            Stream<SecurityLog> stream = infinite(i -> log("10.0.0.1", i % 7 == 6 ? "CRITICAL" : "WARN", i));

            Optional<String> description = analyzer.findFirstCriticalEvent(stream).map(SecurityLog::eventDescription);

            assertEquals(Optional.of("event 6"), description);
        }

        @Test
        void consumesOnlyNecessaryElements() {
            AtomicInteger consumed = new AtomicInteger();
            Stream<SecurityLog> stream = Stream.generate(() -> {
                int i = consumed.getAndIncrement();
                return log("10.0.0.1", i == 3 ? "CRITICAL" : "INFO", i);
            });

            analyzer.findFirstCriticalEvent(stream);

            assertEquals(4, consumed.get());
        }

        @Test
        void returnsEmptyForFiniteStreamWithoutCriticalEvents() {
            Stream<SecurityLog> stream = Stream.of(log("1.1.1.1", "INFO", 0), log("1.1.1.1", "critical", 1));

            assertTrue(analyzer.findFirstCriticalEvent(stream).isEmpty());
        }

        @Test
        void returnsEmptyForEmptyStream() {
            assertTrue(analyzer.findFirstCriticalEvent(Stream.empty()).isEmpty());
        }

        @Test
        void throwsOnNullStream() {
            assertThrows(IllegalArgumentException.class, () -> analyzer.findFirstCriticalEvent(null));
        }
    }

    @Nested
    class GetLogsBeforeDeadline {

        @Test
        void collectsLogsUntilDeadlineFromInfiniteStream() {
            LocalDateTime deadline = START.plusMinutes(5);

            List<SecurityLog> result = analyzer.getLogsBeforeDeadline(infinite(i -> log("1.1.1.1", "INFO", i)), deadline);

            assertEquals(5, result.size());
            assertTrue(result.stream().allMatch(l -> l.timestamp().isBefore(deadline)));
        }

        @Test
        void stopsAtFirstLogNotBeforeDeadline() {
            Stream<SecurityLog> stream = Stream.of(
                    log("1.1.1.1", "INFO", 0),
                    log("1.1.1.1", "INFO", 10),
                    log("1.1.1.1", "INFO", 1)
            );

            List<SecurityLog> result = analyzer.getLogsBeforeDeadline(stream, START.plusMinutes(5));

            assertEquals(List.of(log("1.1.1.1", "INFO", 0)), result);
        }

        @Test
        void returnsEmptyWhenFirstLogIsAfterDeadline() {
            List<SecurityLog> result = analyzer.getLogsBeforeDeadline(infinite(i -> log("1.1.1.1", "INFO", i)), START);

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsAllLogsOfFiniteStreamBeforeDeadline() {
            Stream<SecurityLog> stream = Stream.of(log("1.1.1.1", "INFO", 0), log("1.1.1.1", "INFO", 1));

            assertEquals(2, analyzer.getLogsBeforeDeadline(stream, START.plusDays(1)).size());
        }

        @Test
        void throwsOnNullArguments() {
            assertThrows(IllegalArgumentException.class, () -> analyzer.getLogsBeforeDeadline(null, START));
            assertThrows(IllegalArgumentException.class, () -> analyzer.getLogsBeforeDeadline(Stream.empty(), null));
        }
    }

    @Nested
    class DetectIpBursts {

        @Test
        void detectsBurstsInInfiniteStream() {
            // Each IP sends i % 4 + 1 events in a row: 1, 2, 3, 4, 1, 2, ...
            Stream<SecurityLog> stream = Stream.iterate(0, i -> i + 1)
                    .flatMap(i -> Stream.generate(() -> log("10.0.0." + i, "WARN", i)).limit(i % 4 + 1));

            List<String> result = analyzer.detectIpBursts(stream, 3, 3);

            assertEquals(List.of("10.0.0.2", "10.0.0.3", "10.0.0.6"), result);
        }

        @Test
        void interruptedSeriesIsNotABurst() {
            Stream<SecurityLog> stream = Stream.of("A", "A", "B", "A", "A", "B", "B", "B")
                    .map(ip -> log(ip, "INFO", 0));

            assertEquals(List.of("B"), analyzer.detectIpBursts(stream, 3, 5));
        }

        @Test
        void reportsEachIpOnlyOnce() {
            Stream<SecurityLog> stream = Stream.of("A", "A", "B", "A", "A", "C", "C")
                    .map(ip -> log(ip, "INFO", 0));

            assertEquals(List.of("A", "C"), analyzer.detectIpBursts(stream, 2, 10));
        }

        @Test
        void longSeriesIsReportedOnce() {
            Stream<SecurityLog> stream = Stream.generate(() -> log("A", "INFO", 0)).limit(100);

            assertEquals(List.of("A"), analyzer.detectIpBursts(stream, 2, 10));
        }

        @Test
        void thresholdOfOneReportsEveryNewIp() {
            Stream<SecurityLog> stream = infinite(i -> log("ip-" + i, "INFO", i));

            assertEquals(List.of("ip-0", "ip-1", "ip-2"), analyzer.detectIpBursts(stream, 1, 3));
        }

        @Test
        void stopsConsumingSourceAfterReportIsFull() {
            AtomicInteger consumed = new AtomicInteger();
            Stream<SecurityLog> stream = Stream.generate(() -> {
                int i = consumed.getAndIncrement();
                return log("ip-" + i / 2, "INFO", i);
            });

            List<String> result = analyzer.detectIpBursts(stream, 2, 2);

            assertEquals(List.of("ip-0", "ip-1"), result);
            assertEquals(4, consumed.get());
        }

        @Test
        void returnsAvailableBurstsForFiniteStream() {
            Stream<SecurityLog> stream = Stream.of("A", "A", "B").map(ip -> log(ip, "INFO", 0));

            assertEquals(List.of("A"), analyzer.detectIpBursts(stream, 2, 5));
        }

        @Test
        void throwsOnNullStream() {
            assertThrows(IllegalArgumentException.class, () -> analyzer.detectIpBursts(null, 2, 2));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void throwsOnNonPositiveThreshold(int threshold) {
            assertThrows(IllegalArgumentException.class, () -> analyzer.detectIpBursts(Stream.empty(), threshold, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void throwsOnNonPositiveMaxIps(int maxIps) {
            assertThrows(IllegalArgumentException.class, () -> analyzer.detectIpBursts(Stream.empty(), 1, maxIps));
        }
    }
}
