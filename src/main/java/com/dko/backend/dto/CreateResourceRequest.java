package com.dko.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateResourceRequest {
    private String title;
    private String url;
    private String description;
}
