package dev.leonardo.bvgtransit.commute.service;

import static org.junit.jupiter.api.Assertions.*;

import dev.leonardo.bvgtransit.commute.RabbitMQConfig;
import dev.leonardo.bvgtransit.commute.dto.LiveAlertDto;

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

        var alert =
                new LiveAlertDto(
                        11L,
                        LocalTime.of(8, 30),
                        "station-id-1",
                        "station-id-2",
                        "station-dir-id",
                        "Station Direction",
                        25);
        liveAlertService.sendDelayAlert(alert);
        var received = rabbitTemplate.receiveAndConvert(RabbitMQConfig.LIVE_ALERTS_QUEUE, 5000);
        assertEquals(alert, received);
    }
}
