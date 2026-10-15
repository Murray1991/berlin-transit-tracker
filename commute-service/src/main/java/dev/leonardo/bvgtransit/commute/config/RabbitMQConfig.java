package dev.leonardo.bvgtransit.commute.config;

import dev.leonardo.bvgtransit.commute.dto.LiveAlertDto;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String LIVE_ALERTS_QUEUE = "live-alerts";

    @Bean
    public Queue liveAlertsQueue() {
        return new Queue(LIVE_ALERTS_QUEUE, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter(LiveAlertDto.class.getPackage().getName());
    }
}
