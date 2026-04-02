package com.productfinder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.productfinder.model.Product;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductResponse {

    private Long id;
    private String title;
    private String url;
    private String status;
    private String price;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static ProductResponse from(Product p) {
        ProductResponse r = new ProductResponse();
        r.id = p.getId();
        r.title = p.getTitle();
        r.url = p.getUrl();
        r.status = p.getStatus();
        r.price = p.getPrice();
        r.createdAt = p.getCreatedAt();
        return r;
    }
}
