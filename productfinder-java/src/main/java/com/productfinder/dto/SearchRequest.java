package com.productfinder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SearchRequest {

    @NotBlank(message = "Search query is required")
    @Size(min = 1, max = 200, message = "Query must be 1–200 characters")
    private String query;
}
