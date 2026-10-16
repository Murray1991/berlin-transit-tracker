import json
import os
import sys

import pika

QUEUE_LIVE_ALERTS = "live-alerts"


def handle_message(body: bytes) -> dict:
    return json.loads(body)


def on_message(ch, method, properties, body):
    msg_alert = handle_message(body)
    print(
        f" [x] Received alert for route {msg_alert['routeId']}: {msg_alert['delay']} min of delay"
        % body
    )


class RabbitMQConsumer:
    def __init__(self, connection_params: pika.ConnectionParameters):
        self.connection = pika.BlockingConnection(connection_params)
        self.channel = self.connection.channel()
        self.channel.queue_declare(queue=QUEUE_LIVE_ALERTS, durable=True)

    def start_consuming(self):
        self.channel.basic_consume(
            queue=QUEUE_LIVE_ALERTS, auto_ack=True, on_message_callback=on_message
        )
        self.channel.start_consuming()

    def consume(self):
        method, properties, body = self.channel.basic_get(
            queue=QUEUE_LIVE_ALERTS, auto_ack=True
        )
        if method is None:
            return None
        return handle_message(body)

    def close(self):
        if self.connection.is_open:
            self.connection.close()


def main():
    consumer = RabbitMQConsumer(
        pika.ConnectionParameters(
            host=os.getenv("RABBITMQ_HOST", "localhost"),
            credentials=pika.PlainCredentials(
                os.getenv("RABBITMQ_USER", ""),
                os.getenv("RABBITMQ_PASSWORD", ""),
            ),
        )
    )

    print(" [x] Waiting for messages. To exit press CTRL+C")
    consumer.start_consuming()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("^C received, shutting down")
        sys.exit(0)
