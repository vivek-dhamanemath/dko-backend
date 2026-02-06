package com.dko.backend.controller;

import com.dko.backend.dto.*;
import com.dko.backend.model.User;
import com.dko.backend.repository.UserRepository;
import com.dko.backend.security.JwtUtil;
import com.dko.backend.security.SecurityUtils;
import com.dko.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println("📩 Received registration request for: " + request.getEmail());
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        System.out.println("📩 Received login request for: " + request.getEmail());
        User user = authService.authenticate(request);

        String accessToken = jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        authService.saveRefreshToken(user, refreshToken);

        return ResponseEntity.ok(new AuthResponse(
                accessToken,
                "Bearer",
                jwtUtil.getAccessTokenExpiryInSeconds(),
                refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        System.out.println("📩 Received refresh request");
        return ResponseEntity.ok(authService.refreshAccessToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        System.out.println("📩 Received logout request");
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll() {
        System.out.println("📩 Received logout-all request");
        User user = SecurityUtils.getCurrentUser(userRepository);
        authService.logoutAll(user.getId());
        return ResponseEntity.noContent().build();
    }
}
