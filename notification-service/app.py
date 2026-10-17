import asyncio
import json
import os
import sys
from typing import Awaitable, Callable

import aio_pika

QUEUE_LIVE_ALERTS = "live-alerts"


class RabbitMqSettings:
    def __init__(self, host: str, port: int, login: str, password: str):
        self.host = host
        self.port = int(port)
        self.login = login
        self.password = password

    async def connect(self):
        return await aio_pika.connect_robust(
            host=self.host,
            port=self.port,
            login=self.login,
            password=self.password,
        )


class LiveAlertsConnection:
    def __init__(self, settings: RabbitMqSettings):
        self.settings = settings
        self.connection = None
        self.channel = None
        self.queue = None

    async def __aenter__(self):
        self.connection = await self.settings.connect()
        self.channel = await self.connection.channel()

        await self.channel.set_qos(prefetch_count=10)

        self.queue = await self.channel.declare_queue(QUEUE_LIVE_ALERTS, durable=True)
        return self.connection, self.channel, self.queue

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        await self.connection.close()


async def consume_live_alerts(
    settings: RabbitMqSettings, on_message: Callable[[dict], Awaitable[None]]
):
    async with LiveAlertsConnection(settings) as (_, _, queue):
        async with queue.iterator() as queue_iter:
            async for message in queue_iter:
                async with message.process():
                    await on_message(json.loads(message.body))


async def main() -> None:

    settings = RabbitMqSettings(
        host=os.getenv("RABBITMQ_HOST", "localhost"),
        port=int(os.getenv("RABBITMQ_PORT", "5672")),
        login=os.getenv("RABBITMQ_USER", "guest"),
        password=os.getenv("RABBITMQ_PASSWORD", "guest"),
    )

    async def on_message(msg_alert: dict):
        print(
            f" [x] Received alert for route {msg_alert['routeId']}: {msg_alert['delay']} min of delay"
        )

    await consume_live_alerts(settings, on_message)


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("^C received, shutting down")
        sys.exit(0)
