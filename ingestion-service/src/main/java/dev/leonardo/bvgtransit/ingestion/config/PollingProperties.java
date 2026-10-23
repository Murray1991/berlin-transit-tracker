package dev.leonardo.bvgtransit.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ingestion.polling")
public record PollingProperties(
        int batchSize, int intervalMs, int maxRequestsPerMinute, int maxTripDetailsPerPoll) {

    public PollingProperties {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than 0");
        }
        if (intervalMs <= 0) {
            throw new IllegalArgumentException("intervalMs must be greater than 0");
        }
        if (maxRequestsPerMinute <= 0) {
            throw new IllegalArgumentException("maxRequestsPerMinute must be greater than 0");
        }
        if (maxTripDetailsPerPoll < 0) {
            throw new IllegalArgumentException("maxTripDetailsPerPoll must not be negative");
        }

        double departureRequestsPerMinute = batchSize * (60000.0 / intervalMs);
        double expectedRequestsPerMinute = departureRequestsPerMinute + maxTripDetailsPerPoll + 1;

        if (expectedRequestsPerMinute > maxRequestsPerMinute) {
            throw new IllegalArgumentException(
                    "%.2f expected requests per minute greater than maxRequestsPerMinute = %d"
                            .formatted(expectedRequestsPerMinute, maxRequestsPerMinute));
        }
    }
}
