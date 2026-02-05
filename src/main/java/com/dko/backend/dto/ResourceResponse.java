package com.dko.backend.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceResponse {
    private UUID id;
    private String title;
    private String url;
    private String description;
    private Instant createdAt;
}
