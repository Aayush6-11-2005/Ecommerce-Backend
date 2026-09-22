package com.example.Ecommerce.repository;

import com.example.Ecommerce.entity.RefreshToken;
import com.example.Ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUser(User user);

    void deleteByTokenHash(String tokenHash);
}