package dev.leonardo.bvgtransit.ingestion;

import static org.junit.jupiter.api.Assertions.*;

import dev.leonardo.bvgtransit.ingestion.bvg.BvgClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.Duration;

class BvgClientTest {
    private MockWebServer server;
    private BvgClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        var webClient = WebClient.builder().baseUrl(server.url("/").toString()).build();
        client = new BvgClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void propagatesServerError() {
        server.enqueue(new MockResponse().setResponseCode(503));

        WebClientResponseException exception =
                assertThrows(
                        WebClientResponseException.class,
                        () -> client.fetchDepartures("900000").block(Duration.ofSeconds(2)));

        assertEquals(503, exception.getStatusCode().value());
    }

    @Test
    void propagatesNotFound() {
        server.enqueue(new MockResponse().setResponseCode(404));

        WebClientResponseException exception =
                assertThrows(
                        WebClientResponseException.class,
                        () -> client.fetchDepartures("900000").block(Duration.ofSeconds(2)));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void requestDepartures() throws InterruptedException {
        String responseBody = "{\"departures\": [], \"reaktuneDataYodatedAt\": 12345 }";
        server.enqueue(
                new MockResponse()
                        .setHeader("content-type", "application/json; charset=utf-8")
                        .setBody(responseBody));

        String result = client.fetchDepartures("900000").block(Duration.ofSeconds(2));
        assertEquals(responseBody, result);

        RecordedRequest request = server.takeRequest();
        assertNotNull(request);
        assertEquals("GET", request.getMethod());
        assertEquals(
                "/stops/900000/departures?tram=false&bus=false&ferry=false&express=false&regional=false",
                request.getPath());
    }

    @Test
    void requestTrips() throws InterruptedException {
        String responseBody = "{\"trips\": [] }";
        server.enqueue(
                new MockResponse()
                        .setHeader("content-type", "application/json; charset=utf-8")
                        .setBody(responseBody));

        String result = client.fetchTrips().block(Duration.ofSeconds(2));
        assertEquals(responseBody, result);

        RecordedRequest request = server.takeRequest();
        assertNotNull(request);
        assertEquals("GET", request.getMethod());
        assertEquals(
                "/trips?tram=false&bus=false&ferry=false&express=false&regional=false&stopovers=true&operatorNames=Berliner%2520Verkehrsbetriebe,S-Bahn%2520Berlin%2520GmbH",
                request.getPath());
    }
}
