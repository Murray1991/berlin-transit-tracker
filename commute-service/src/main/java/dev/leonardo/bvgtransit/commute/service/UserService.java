package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.dto.UserDto;
import dev.leonardo.bvgtransit.commute.model.Route;
import dev.leonardo.bvgtransit.commute.model.User;
import dev.leonardo.bvgtransit.commute.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository_) {
        userRepository = userRepository_;
    }

    @Transactional
    public UserDto.Response createUser() {
        User user = userRepository.save(new User());
        return new UserDto.Response(user.getPublicId());
    }

    @Transactional
    public void addRoute(UUID publicId, Route route) {
        User user =
                userRepository
                        .findByPublicId(publicId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "User not found"));
        route.addUser(user);
        user.addRoute(route);
    }
}
