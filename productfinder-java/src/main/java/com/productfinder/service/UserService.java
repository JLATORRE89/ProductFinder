package com.productfinder.service;

import com.productfinder.dto.LoginRequest;
import com.productfinder.dto.RegisterRequest;
import com.productfinder.dto.TokenResponse;
import com.productfinder.dto.UserResponse;
import com.productfinder.model.User;
import com.productfinder.repository.UserRepository;
import com.productfinder.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        String username = req.getUsername().trim();
        String email = req.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .hashedPassword(passwordEncoder.encode(req.getPassword()))
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user: {}", saved.getUsername());
        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername().trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Incorrect username or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getHashedPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }

        String token = jwtUtil.generateToken(user.getId());
        log.info("User logged in: {}", user.getUsername());
        return new TokenResponse(token);
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(User user) {
        return UserResponse.from(user);
    }
}
