package com.event.EventManage.service;

import com.event.EventManage.dto.AuthResponse;
import com.event.EventManage.dto.LoginRequest;
import com.event.EventManage.dto.RegisterRequest;
import com.event.EventManage.exception.BadRequestException;
import com.event.EventManage.model.RefreshToken;
import com.event.EventManage.model.Role;
import com.event.EventManage.model.User;
import com.event.EventManage.repository.UserRepository;
import com.event.EventManage.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info("Token generated successfully for user: {}", loginRequest.getEmail());
        return new AuthResponse(token, refreshToken.getToken());
    }

    public User register(RegisterRequest registerRequest) {
        log.info("Registering new user with email: {}", registerRequest.getEmail());
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} is already in use", registerRequest.getEmail());
            throw new BadRequestException("Email Address already in use!");
        }

        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .phone(registerRequest.getPhone())
                .role(Role.CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }
}
