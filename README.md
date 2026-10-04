# Berlin Transit Tracker

The Berlin Transit Tracker wants to be a polyglot microservices application. The system should monitor the Berlin public transport network for delays and notify users in real time.

This project serves as a more-or-less comprehensive playground for learning relevant technologies in today's microservices world.

# The Architecture

The system is composed of distinct microservices communicating through REST APIs, backed by different data technologies to handle specific workloads. The initial idea of the architecture is given by the following depiction.

```
┌───────────────┐
│ BVG REST API  │
└──────▲────────┘
       │
       │
       │Polling
┌──────┴───────────────────┐ Writes         ┌───────────┐
│ Transit Ingestion Service├────────────────►   Redis   │
│                          │                └─────▲─────┘
└──────────────────────────┘                      │
                                                  │ Reads from
┌──────────┐       ┌────────────┐       ┌─────────┴───────────┐
│ Frontend ├───────►API Gateway ├───────►   Commute Service   │
└────▲─────┘       └────────────┘       └─────────┬───────────┘
     │                                            │
     │                                  ┌─────────┴───────────┐
     │                                  │                     │
     │                                  │ Publishes           │ Reads/Writes
┌────┴─────────┐      Subscribes ┌──────▼───────┐         ┌───▼───────────┐
│ Notification ◄─────────────────┤Message Broker│         │PostgreSQL (DB)│
│ Service      │                 └──────────────┘         └───────────────┘
└──────────────┘
```

### Transit Ingestion Service

It is the data fetcher and cacher. Periodically, it polls transit data for specific train stations from BVG APIs, and immediately stores the transit data in Redis. Because of rate limits of BVG APIs, this ensures rate-limit-free data access for the rest of the system.

### Commute Service

It manages the saved commute routes of the users, storing them permanently in a PostgreSQL database. It continuously reads the transit data cached in Redis: if it detects that a train matching a saved route is delayed, it generates a ``Delay'' event and publishes it to the Message Broker (RabbitMQ or Apache Kafka).

### Notification Service

It connects to the Message Broker and listens for delay events. If an event is consumed, it pushes the alert to the connected users via WebSockets.

### API Gateway

It intecepts API calls and routes them to the appropriate service.

### Frontend

The frontend will probably written with plain HTML/Javascript. The UI should be a basic form with a button "Save" and three fields:

1. Start station
2. End station
3. Departure Time

I don't want to lose my mind too much on authentication and security aspects. So users do not create accounts or passwords, the frontend generates a unique ID that will be stored in local storage. The ID will be passed in API calls to link a user to their saved daily commute routes.
