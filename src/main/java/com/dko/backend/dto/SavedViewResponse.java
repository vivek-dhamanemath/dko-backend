package com.dko.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SavedViewResponse {
    private UUID id;
    private String name;
    private FilterCriteria filters;
    private Instant createdAt;
}
