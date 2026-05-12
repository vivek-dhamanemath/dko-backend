package com.dko.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class UpdateResourceRequest {
    @NotBlank(message = "URL is required")
    @Size(max = 2048, message = "URL must be less than 2048 characters")
    private String url;

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be less than 500 characters")
    private String title;

    @Size(max = 1000, message = "Note must be less than 1000 characters")
    private String note;

    @Size(max = 100, message = "Category must be less than 100 characters")
    private String category;

    @Size(max = 20, message = "Maximum 20 tags allowed")
    private List<@Size(max = 50, message = "Tag must be less than 50 characters") String> tags;

    @Size(max = 50, message = "Icon name must be less than 50 characters")
    private String icon;
}
