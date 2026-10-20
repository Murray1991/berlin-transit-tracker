package dev.leonardo.bvgtransit.ingestion;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Service
public class StationCatalog {

    private final List<StationDTO> stations;

    public StationCatalog(
            ObjectMapper objectMapper,
            @Value("classpath:data/stations.json") Resource stationsResource)
            throws IOException {

        try (var inputStream = stationsResource.getInputStream()) {
            stations = List.copyOf(objectMapper.readValue(inputStream, new TypeReference<>() {}));
        }

        if (stations.isEmpty()) {
            throw new IllegalStateException("Station data is empty: " + stationsResource);
        }
    }

    public List<StationDTO> getStations() {
        return stations;
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public record StationDTO(String id, String name, List<String> companyCodes) {}
}
