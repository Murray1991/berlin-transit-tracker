package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.dto.LiveTransitDto;
import dev.leonardo.bvgtransit.commute.model.Route;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LiveAlertService {
    public void sendDelayAlert(Route route, LiveTransitDto liveTransitDto) {
        log.info(
                "Valid transit data (direction {} at {}) for route {} has a delay of {}",
                liveTransitDto.direction(),
                liveTransitDto.plannedWhen(),
                route.getId(),
                liveTransitDto.delay());
    }
}
