package dev.leonardo.bvgtransit.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StationPollingService {
    private static final Logger log = LoggerFactory.getLogger(StationPollingService.class);

    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final List<StationRepository.StationDTO> stations;
    private final Integer BATCH_SIZE = 25;

    private final WebClient bvgWebClient;

    public StationPollingService(
            StationRepository stationRepository,
            @Value("${bvg.api.scheme:http}") String scheme,
            @Value("${bvg.api.host}") String host,
            @Value("${bvg.api.port}") int port) {
        stations = stationRepository.getStations();
        String bvgBaseURL =
                UriComponentsBuilder.newInstance()
                        .scheme(scheme)
                        .host(host)
                        .port(port)
                        .build()
                        .toUriString();
        bvgWebClient = WebClient.builder().baseUrl(bvgBaseURL).build();
    }

    @Scheduled(fixedRate = 15000)
    public void pollDepartures() {
        int start = currentIndex.getAndUpdate(x -> (x + BATCH_SIZE) % stations.size());
        log.info("Polling {} stations from index {}", BATCH_SIZE, start);
        for (int i = 0; i < BATCH_SIZE; i++) {
            int index = (start + i) % stations.size();
            var station = stations.get(index);
            bvgWebClient
                    .get()
                    .uri(
                            "/stops/"
                                    + station.id()
                                    + "/departures?tram=false&bus=false&ferry=false&express=false&regional=false")
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            json -> log.info("{} - {} - {}", index, station.id(), json),
                            error -> log.error(error.getMessage(), error));
        }
    }
}
