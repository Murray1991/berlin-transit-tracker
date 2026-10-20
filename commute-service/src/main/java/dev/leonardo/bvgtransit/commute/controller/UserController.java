package dev.leonardo.bvgtransit.commute.controller;

import dev.leonardo.bvgtransit.commute.dto.UserDto;
import dev.leonardo.bvgtransit.commute.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService_) {
        userService = userService_;
    }

    @PostMapping
    public ResponseEntity<UserDto.Response> createUser() {
        var response = userService.createUser();
        URI location = URI.create("/api/users/" + response.uuid());
        return ResponseEntity.created(location).body(response);
    }
}
