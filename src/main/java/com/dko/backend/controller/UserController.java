package com.dko.backend.controller;

import com.dko.backend.dto.UserResponse;
import com.dko.backend.model.User;
import com.dko.backend.repository.UserRepository;
import com.dko.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow();

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt());
    }
}
