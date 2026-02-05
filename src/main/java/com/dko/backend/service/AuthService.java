package com.dko.backend.service;

import com.dko.backend.dto.AuthResponse;
import com.dko.backend.dto.LoginRequest;
import com.dko.backend.dto.RegisterRequest;
import com.dko.backend.model.RefreshToken;
import com.dko.backend.model.Role;
import com.dko.backend.model.User;
import com.dko.backend.repository.RefreshTokenRepository;
import com.dko.backend.repository.UserRepository;
import com.dko.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now());

        userRepository.save(user);
    }

    public User authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    @Transactional
    public void saveRefreshToken(User user, String token) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(Instant.now());

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Check if token is revoked
        if (storedToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        // Check if token is expired
        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token has expired");
        }

        // Validate JWT structure
        jwtUtil.validateToken(refreshToken);

        User user = storedToken.getUser();

        // Token rotation: revoke old token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getRole().name());

        // Generate new refresh token
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        RefreshToken newStoredToken = new RefreshToken();
        newStoredToken.setToken(newRefreshToken);
        newStoredToken.setUser(user);
        newStoredToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        newStoredToken.setRevoked(false);
        newStoredToken.setCreatedAt(Instant.now());

        refreshTokenRepository.save(newStoredToken);

        return new AuthResponse(
                newAccessToken,
                "Bearer",
                jwtUtil.getAccessTokenExpiryInSeconds(),
                newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void logoutAll(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
