package dev.leonardo.bvgtransit.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisStartupCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RedisStartupCheck.class);
    private final ReactiveRedisConnectionFactory connectionFactory;

    public RedisStartupCheck(ReactiveRedisConnectionFactory connectionFactory_) {
        connectionFactory = connectionFactory_;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String response =
                    connectionFactory.getReactiveConnection().ping().block(Duration.ofSeconds(5));
            assert response != null;
            if (!response.equalsIgnoreCase("pong")) {
                throw new IllegalStateException("Unexpected Redis ping response: " + response);
            }
            log.info("Redis startup check successful");
        } catch (Exception ex) {
            log.error("Redis is unavailable or its configuration is invalid", ex);
        }
    }
}
