package com.event.EventManage.service;

import com.event.EventManage.exception.TokenRefreshException;
import com.event.EventManage.model.RefreshToken;
import com.event.EventManage.repository.RefreshTokenRepository;
import com.event.EventManage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(String userId) {
        com.event.EventManage.model.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Created/Updated refresh token for user ID: {}", userId);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            log.warn("Refresh token expired for user ID: {}", token.getUser().getId());
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(String userId) {
        log.info("Deleting refresh token for user ID: {}", userId);
        return userRepository.findById(userId).map(user -> {
            refreshTokenRepository.deleteByUser(user);
            return 1;
        }).orElse(0);
    }

    @Transactional
    public int deleteByUserEmail(String email) {
        log.info("Deleting refresh token for user email: {}", email);
        return userRepository.findByEmail(email).map(user -> {
            refreshTokenRepository.deleteByUser(user);
            return 1;
        }).orElse(0);
    }
}
