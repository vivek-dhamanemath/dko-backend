package com.dko.backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateResourceRequest {
    private String url;
    private String title;
    private String note;
    private String category;
    private List<String> tags;
}
