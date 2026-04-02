package com.productfinder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.productfinder.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String email;
    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.email = user.getEmail();
        r.username = user.getUsername();
        r.createdAt = user.getCreatedAt();
        return r;
    }
}
