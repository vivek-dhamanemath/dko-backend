package com.dko.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "supabase_id", nullable = false, unique = true)
    private String supabaseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "lifetime_resources_count", nullable = false, columnDefinition = "bigint default 0")
    private long lifetimeResourcesCount = 0;
}
