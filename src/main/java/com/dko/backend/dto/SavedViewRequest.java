package com.dko.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedViewRequest {
    private String name;
    private FilterCriteria filters;
}
