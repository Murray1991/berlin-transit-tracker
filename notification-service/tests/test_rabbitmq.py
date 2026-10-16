import json
import unittest

import pika
from testcontainers.community.rabbitmq import RabbitMqContainer

from app import QUEUE_LIVE_ALERTS, handle_message


class TestRabbitmq(unittest.TestCase):
    def test(self):
        with RabbitMqContainer(
            "rabbitmq:4.3.6", username="test", password="test"
        ) as rabbit:
            connection = pika.BlockingConnection(rabbit.get_connection_params())
            try:
                channel = connection.channel()
                channel.queue_declare(queue=QUEUE_LIVE_ALERTS, durable=True)
                expected = {
                    "routeId": 11,
                    "plannedWhen": "08:30",
                    "fromId": "station-id-1",
                    "toId": "station-id-2",
                    "directionId": "direction-id",
                    "directionName": "Destination",
                    "delay": 1,
                }

                channel.basic_publish(
                    exchange="",
                    routing_key=QUEUE_LIVE_ALERTS,
                    body=json.dumps(expected).encode(),
                )
                method, properties, body = channel.basic_get(
                    queue=QUEUE_LIVE_ALERTS, auto_ack=True
                )

                self.assertIsNotNone(method)
                self.assertEqual(expected, handle_message(body))
            finally:
                connection.close()


if __name__ == "__main__":
    unittest.main()
