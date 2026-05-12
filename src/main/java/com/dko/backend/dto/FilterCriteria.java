package com.dko.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class FilterCriteria {
    private List<String> categories;
    private List<String> tags;
    private String dateRange; // "last_7_days", "last_30_days", "last_90_days", "custom"
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isArchived;
    private java.util.UUID collectionId;

    @Min(value = 0, message = "Page must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "Size must be >= 1")
    @Max(value = 100, message = "Size must be <= 100")
    private int size = 20;
}
