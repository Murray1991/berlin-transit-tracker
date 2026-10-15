package dev.leonardo.bvgtransit.commute;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String LIVE_ALERTS_QUEUE = "live-alerts";

    @Bean
    public Queue liveAlertsQueue() {
        return new Queue(LIVE_ALERTS_QUEUE, true);
    }
}
