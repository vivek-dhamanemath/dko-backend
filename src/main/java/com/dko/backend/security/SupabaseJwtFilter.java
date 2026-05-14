package com.dko.backend.security;

import com.dko.backend.model.Role;
import com.dko.backend.model.User;
import com.dko.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

@Component
public class SupabaseJwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SupabaseJwtFilter.class);

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;

    public SupabaseJwtFilter(JwtDecoder jwtDecoder, UserRepository userRepository) {
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(token);

                String supabaseId = jwt.getSubject();
                String email = jwt.getClaimAsString("email");

                if (supabaseId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Auto-provision user on first request
                    User user = userRepository.findBySupabaseId(supabaseId)
                            .orElseGet(() -> {
                                User newUser = new User();
                                newUser.setSupabaseId(supabaseId);
                                newUser.setEmail(email != null ? email : "unknown");
                                newUser.setRole(Role.USER);
                                newUser.setCreatedAt(Instant.now());
                                log.info("Auto-provisioned new user for Supabase ID: {}", supabaseId);
                                return userRepository.save(newUser);
                            });

                    // Update email if it changed in Supabase
                    if (email != null && !email.equals(user.getEmail())) {
                        user.setEmail(email);
                        userRepository.save(user);
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.getId().toString(),
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Supabase auth successful for user: {} [{}]", user.getId(), user.getRole());
                }
            } catch (Exception e) {
                log.warn("Supabase JWT validation failed: {}", e.getMessage());
                log.debug("JWT validation error details:", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
