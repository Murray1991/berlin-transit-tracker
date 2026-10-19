import asyncio
import json
import unittest
from contextlib import suppress

import aio_pika
from testcontainers.community.rabbitmq import RabbitMqContainer

from app import RabbitMqSettings, LiveAlertsConnection, consume_live_alerts


class TestRabbitmq(unittest.IsolatedAsyncioTestCase):

    async def test(self):
        with RabbitMqContainer(
            "rabbitmq:4.3.6", username="test", password="test"
        ) as rabbit:
            params = rabbit.get_connection_params()
            settings = RabbitMqSettings(
                host=params.host,
                port=params.port,
                login="test",
                password="test",
            )

            expected = {
                "routeId": 11,
                "plannedWhen": "08:30",
                "fromId": "station-id-1",
                "toId": "station-id-2",
                "directionId": "direction-id",
                "directionName": "Destination",
                "delay": 1,
                "userIds": ["aaa", "bbb", "ccc"]
            }

            received = asyncio.Queue()

            async def handler(msg_alert: dict):
                await received.put(msg_alert)

            consumer_task = asyncio.create_task(
                consume_live_alerts(settings, on_message=handler)
            )

            try:
                async with LiveAlertsConnection(settings) as (
                    _,
                    channel,
                    queue,
                ):
                    await channel.default_exchange.publish(
                        aio_pika.Message(body=json.dumps(expected).encode()),
                        routing_key=queue.name,
                    )
                    actual = await asyncio.wait_for(received.get(), timeout=5)
                    self.assertEqual(expected, actual)
            finally:
                consumer_task.cancel()
                with suppress(asyncio.CancelledError):
                    await consumer_task


if __name__ == "__main__":
    unittest.main()
