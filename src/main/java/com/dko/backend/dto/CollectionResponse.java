package com.dko.backend.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollectionResponse {
    private UUID id;
    private String name;
    private Instant createdAt;
}
