package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.dto.UserDto;
import dev.leonardo.bvgtransit.commute.model.User;
import dev.leonardo.bvgtransit.commute.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

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
}
