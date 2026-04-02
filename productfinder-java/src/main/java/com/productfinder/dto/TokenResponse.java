package com.productfinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String tokenType = "bearer";

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
