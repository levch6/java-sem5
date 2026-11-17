package ua.kpi.comsys.streams;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class SecurityLogAnalyzer {

    private static final String CRITICAL = "CRITICAL";

    public Optional<SecurityLog> findFirstCriticalEvent(Stream<SecurityLog> stream) {
        return requireNonNull(stream, "stream")
                .filter(log -> CRITICAL.equals(log.severityLevel()))
                .findFirst();
    }

    public List<SecurityLog> getLogsBeforeDeadline(Stream<SecurityLog> stream, LocalDateTime deadline) {
        Stream<SecurityLog> source = requireNonNull(stream, "stream");
        LocalDateTime limit = requireNonNull(deadline, "deadline");

        return source
                .takeWhile(log -> log.timestamp().isBefore(limit))
                .toList();
    }

    public List<String> detectIpBursts(Stream<SecurityLog> stream, int burstThreshold, int maxIpsToReport) {
        Stream<SecurityLog> source = requireNonNull(stream, "stream");
        requirePositive(burstThreshold, "burstThreshold");
        requirePositive(maxIpsToReport, "maxIpsToReport");

        return source
                .gather(burstDetector(burstThreshold, maxIpsToReport))
                .toList();
    }

    private static Gatherer<SecurityLog, ?, String> burstDetector(int burstThreshold, int maxIpsToReport) {
        class State {
            String currentIp;
            int streak;
            final Set<String> reported = new HashSet<>();
        }

        return Gatherer.ofSequential(
                State::new,
                Gatherer.Integrator.<State, SecurityLog, String>of((state, log, downstream) -> {
                    if (Objects.equals(state.currentIp, log.ipAddress())) {
                        state.streak++;
                    } else {
                        state.currentIp = log.ipAddress();
                        state.streak = 1;
                    }

                    if (state.streak == burstThreshold && state.reported.add(state.currentIp)) {
                        if (!downstream.push(state.currentIp)) {
                            return false;
                        }
                    }
                    // Stop pulling from the source once the report is full.
                    return state.reported.size() < maxIpsToReport;
                })
        );
    }

    private static <T> T requireNonNull(T value, String name) {
        return Optional.ofNullable(value)
                .orElseThrow(() -> new IllegalArgumentException(name + " cannot be null"));
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than 0");
        }
    }
}
