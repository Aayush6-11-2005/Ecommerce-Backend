package com.example.Ecommerce.services;

import com.example.Ecommerce.entity.RefreshToken;
import com.example.Ecommerce.entity.User;
import com.example.Ecommerce.exception.BadRequestException;
import com.example.Ecommerce.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository) {

        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String createRefreshToken(User user) {

        refreshTokenRepository.deleteByUser(user);

        String rawToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setTokenHash(hashToken(rawToken));

        refreshToken.setExpiryDate(
                LocalDateTime.now()
                        .plusSeconds(refreshExpiration / 1000)
        );

        refreshToken.setUser(user);

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RefreshToken verifyAndGet(String rawToken) {

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            refreshTokenRepository.delete(refreshToken);

            throw new BadRequestException(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }

    @Transactional
    public String rotateRefreshToken(
            RefreshToken oldRefreshToken) {

        User user = oldRefreshToken.getUser();

        refreshTokenRepository.delete(oldRefreshToken);

        return createRefreshToken(user);
    }

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    e
            );
        }
    }
}