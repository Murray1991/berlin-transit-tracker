import json
import os
import sys

import pika

QUEUE_LIVE_ALERTS = "live-alerts"


def handle_message(body: bytes) -> dict:
    return json.loads(body)


def main():
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(
            host=os.getenv("RABBITMQ_HOST", "localhost"),
            credentials=pika.PlainCredentials(
                os.getenv("RABBITMQ_USER", ""),
                os.getenv("RABBITMQ_PASSWORD", ""),
            ),
        )
    )
    channel = connection.channel()

    channel.queue_declare(queue=QUEUE_LIVE_ALERTS, durable=True)

    def on_message(ch, method, properties, body):
        msg_alert = handle_message(body)
        print(
            f" [x] Received alert for route {msg_alert['routeId']}: {msg_alert['delay']} min of delay"
            % body
        )

    channel.basic_consume(
        queue=QUEUE_LIVE_ALERTS, auto_ack=True, on_message_callback=on_message
    )
    print(" [x] Waiting for messages. To exit press CTRL+C")
    channel.start_consuming()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("^C received, shutting down")
        sys.exit(0)
