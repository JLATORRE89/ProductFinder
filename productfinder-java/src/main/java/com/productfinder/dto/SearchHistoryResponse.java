package com.productfinder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.productfinder.model.Search;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchHistoryResponse {

    private Long id;
    private String query;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private int productCount;

    public static SearchHistoryResponse from(Search search) {
        SearchHistoryResponse r = new SearchHistoryResponse();
        r.id = search.getId();
        r.query = search.getQuery();
        r.createdAt = search.getCreatedAt();
        r.productCount = search.getProducts().size();
        return r;
    }
}
