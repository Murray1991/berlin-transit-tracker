package dev.leonardo.bvgtransit.commute.service;

import static org.junit.jupiter.api.Assertions.*;

import dev.leonardo.bvgtransit.commute.config.RabbitMQConfig;
import dev.leonardo.bvgtransit.commute.dto.LiveAlertDto;
import dev.leonardo.bvgtransit.commute.dto.LiveTransitDto;
import dev.leonardo.bvgtransit.commute.model.Route;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.LocalTime;

@SpringBootTest
@Testcontainers
class LiveAlertServiceRabbitTest {

    @Container @ServiceConnection
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Container @ServiceConnection
    private static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4.3.6");

    @Autowired LiveAlertService liveAlertService;
    @Autowired RabbitTemplate rabbitTemplate;

    @Test
    void sendsAlertToLiveAlerts() {
        LocalTime time = LocalTime.now();
        Route route = CommuteServiceTest.createRoute("station-1", "station-2", time);
        LiveTransitDto ltd = CommuteServiceTest.createLiveTransitData(time, route, 0);
        LiveAlertDto alert = CommuteService.createLiveAlertDto(route, ltd);
        liveAlertService.sendLiveAlert(alert);

        var received = rabbitTemplate.receiveAndConvert(RabbitMQConfig.LIVE_ALERTS_QUEUE, 5000);
        assertEquals(alert, received);
    }
}
