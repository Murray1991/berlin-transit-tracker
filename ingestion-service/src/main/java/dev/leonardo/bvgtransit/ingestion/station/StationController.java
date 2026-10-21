package dev.leonardo.bvgtransit.ingestion.station;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {
    private final StationCatalog stationCatalog;

    public StationController(StationCatalog stationCatalog_) {
        stationCatalog = stationCatalog_;
    }

    @GetMapping
    public ResponseEntity<List<StationResponseDto>> getStations() {
        return ResponseEntity.ok(
                stationCatalog.getStations().stream()
                        .map(
                                station ->
                                        new StationResponseDto(
                                                station.id(),
                                                station.name(),
                                                station.companyCodes()))
                        .toList());
    }

    public record StationResponseDto(String id, String name, List<String> companyCodes) {}
}
