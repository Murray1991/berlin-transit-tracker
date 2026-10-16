import json
import unittest

from testcontainers.community.rabbitmq import RabbitMqContainer

from app import QUEUE_LIVE_ALERTS, RabbitMQConsumer


class TestRabbitmq(unittest.TestCase):
    def test(self):
        with RabbitMqContainer(
            "rabbitmq:4.3.6", username="test", password="test"
        ) as rabbit:
            consumer = RabbitMQConsumer(rabbit.get_connection_params())

            expected = {
                "routeId": 11,
                "plannedWhen": "08:30",
                "fromId": "station-id-1",
                "toId": "station-id-2",
                "directionId": "direction-id",
                "directionName": "Destination",
                "delay": 1,
            }

            consumer.channel.basic_publish(
                exchange="",
                routing_key=QUEUE_LIVE_ALERTS,
                body=json.dumps(expected).encode(),
            )
            msg = consumer.consume()
            self.assertEqual(expected, msg)

            consumer.close()


if __name__ == "__main__":
    unittest.main()
