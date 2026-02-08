package com.dko.backend.dto;

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
}
