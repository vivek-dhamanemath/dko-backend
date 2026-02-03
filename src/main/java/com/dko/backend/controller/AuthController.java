package com.dko.backend.controller;

import com.dko.backend.dto.*;
import com.dko.backend.model.User;
import com.dko.backend.security.JwtUtil;
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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user = authService.authenticate(request);
        String token = jwtUtil.generateToken(user.getId().toString());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
