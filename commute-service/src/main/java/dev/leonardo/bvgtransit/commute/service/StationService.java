package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.model.Station;
import dev.leonardo.bvgtransit.commute.repository.StationRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StationService {

    private final StationRepository stationRepository;
    private final StationCatalogClient stationClient;

    public StationService(
            StationRepository stationRepository_, StationCatalogClient stationClient_) {
        stationRepository = stationRepository_;
        stationClient = stationClient_;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncAtStart() {
        syncStations();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void syncDaily() {
        syncStations();
    }

    private void syncStations() {
        log.info("Syncing stations");
        try {
            Map<String, Station> existing = new HashMap<>();
            List<StationResponse> catalog = stationClient.fetchStations();
            stationRepository
                    .findAll()
                    .forEach(
                            station -> {
                                existing.put(station.getId(), station);
                            });

            stationRepository.saveAll(
                    catalog.stream()
                            .map(
                                    item -> {
                                        Station s =
                                                existing.computeIfAbsent(
                                                        item.id(), ignored -> new Station());
                                        s.setId(item.id());
                                        s.setName(item.name());
                                        return s;
                                    })
                            .toList());
        } catch (Exception ex) {
            log.error("Could not sync stations", ex);
        }
    }

    @Component
    public static class StationCatalogClient {
        private final RestClient restClient;

        public StationCatalogClient(@Value("${commute.station-catalog.base-url}") String baseUrl) {
            restClient = RestClient.builder().baseUrl(baseUrl).build();
        }

        List<StationResponse> fetchStations() {
            List<StationResponse> stations =
                    restClient
                            .get()
                            .uri("/api/stations")
                            .retrieve()
                            .body(new ParameterizedTypeReference<List<StationResponse>>() {});
            if (stations == null || stations.isEmpty()) {
                throw new IllegalStateException("No stations found");
            }

            return stations;
        }
    }

    public record StationResponse(String id, String name, List<String> companyCodes) {}
}
