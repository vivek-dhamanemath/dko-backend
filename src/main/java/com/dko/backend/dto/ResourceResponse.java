package com.dko.backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceResponse {
    private UUID id;
    private String url;
    private String title;
    private String note;
    private String category;
    private List<String> tags;
    private Instant createdAt;
    @JsonProperty("isArchived")
    private boolean isArchived;
    @JsonProperty("isDeleted")
    private boolean isDeleted;
    @JsonProperty("isPinned")
    private boolean isPinned;
    private Instant deletedAt;
    private List<CollectionResponse> collections;
}
