package com.dko.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private String category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isArchived = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isPinned = false;

    @OneToMany(mappedBy = "resource", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResourceTag> resourceTags = new HashSet<>();

    @ManyToMany(mappedBy = "resources", fetch = FetchType.LAZY)
    private Set<Collection> collections = new HashSet<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }
}
