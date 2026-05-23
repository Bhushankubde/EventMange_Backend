package com.event.EventManage.controller;

import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.dto.AuthResponse;
import com.event.EventManage.dto.LoginRequest;
import com.event.EventManage.dto.RegisterRequest;
import com.event.EventManage.dto.TokenRefreshRequest;
import com.event.EventManage.dto.TokenRefreshResponse;
import com.event.EventManage.exception.TokenRefreshException;
import com.event.EventManage.model.RefreshToken;
import com.event.EventManage.model.User;
import com.event.EventManage.security.JwtTokenProvider;
import com.event.EventManage.service.AuthService;
import com.event.EventManage.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getEmail());
        AuthResponse authResponse = authService.login(loginRequest);
        log.info("User {} successfully logged in", loginRequest.getEmail());
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful", HttpStatus.OK.value()));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Attempting registration for user: {}", registerRequest.getEmail());
        User user = authService.register(registerRequest);
        AuthResponse authResponse = authService.login(new LoginRequest(registerRequest.getEmail(), registerRequest.getPassword()));
        log.info("User {} successfully registered and logged in", registerRequest.getEmail());
        return new ResponseEntity<>(ApiResponse.success(authResponse, "User registered successfully", HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtTokenProvider.generateTokenFromUsername(user.getEmail(), user.getRole().name());
                    return ResponseEntity.ok(ApiResponse.success(new TokenRefreshResponse(token, requestRefreshToken), "Token refreshed successfully", HttpStatus.OK.value()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            refreshTokenService.deleteByUserEmail(email);
            log.info("User {} successfully logged out", email);
            return ResponseEntity.ok(ApiResponse.success(null, "Log out successful", HttpStatus.OK.value()));
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Log out successful", HttpStatus.OK.value()));
    }
}
