package com.productfinder.controller;

import com.productfinder.dto.*;
import com.productfinder.model.User;
import com.productfinder.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** POST /api/auth/register — create a new account */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        UserResponse user = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /** POST /api/auth/login — exchange credentials for a JWT */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        TokenResponse token = userService.login(req);
        return ResponseEntity.ok(token);
    }

    /** GET /api/auth/me — return the currently authenticated user */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getMe(user));
    }


}
