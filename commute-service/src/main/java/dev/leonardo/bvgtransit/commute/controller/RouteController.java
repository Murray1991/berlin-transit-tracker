package dev.leonardo.bvgtransit.commute.controller;

import dev.leonardo.bvgtransit.commute.dto.RouteDto;
import dev.leonardo.bvgtransit.commute.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService_) {
        routeService = routeService_;
    }

    @PostMapping
    public ResponseEntity<RouteDto.Response> saveRoute(Long userId, @RequestBody RouteDto.Request routeDto) {
        var response = routeService.saveRoute(userId, routeDto);
        return ResponseEntity.ok(response);
    }
}
