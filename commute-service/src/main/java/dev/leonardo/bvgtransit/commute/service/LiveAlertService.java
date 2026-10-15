package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.RabbitMQConfig;
import dev.leonardo.bvgtransit.commute.dto.LiveAlertDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LiveAlertService {
    private final RabbitMessagingTemplate rabbitMessagingTemplate;

    public LiveAlertService(RabbitMessagingTemplate rabbitMessagingTemplate_) {
        rabbitMessagingTemplate = rabbitMessagingTemplate_;
    }

    public void sendDelayAlert(LiveAlertDto liveAlertDto) {
        log.info(
                "Valid transit data (direction {} at {}) for route {} has a delay of {}",
                liveAlertDto.directionName(),
                liveAlertDto.plannedWhen(),
                liveAlertDto.routeId(),
                liveAlertDto.delay());
        rabbitMessagingTemplate.convertAndSend(RabbitMQConfig.LIVE_ALERTS_QUEUE, liveAlertDto);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
