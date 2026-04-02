package com.productfinder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.productfinder.model.Search;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SearchResponse {

    private Long id;
    private String query;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private List<ProductResponse> products;

    public static SearchResponse from(Search search) {
        SearchResponse r = new SearchResponse();
        r.id = search.getId();
        r.query = search.getQuery();
        r.createdAt = search.getCreatedAt();
        r.products = search.getProducts().stream()
                .map(ProductResponse::from)
                .toList();
        return r;
    }
}
