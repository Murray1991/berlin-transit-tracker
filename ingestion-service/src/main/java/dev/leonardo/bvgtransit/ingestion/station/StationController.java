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
    public ResponseEntity<List<StationCatalog.StationDTO>> getStations() {
        return ResponseEntity.ok(stationCatalog.getStations());
    }
}
