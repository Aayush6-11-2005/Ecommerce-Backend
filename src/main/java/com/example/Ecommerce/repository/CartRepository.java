package com.example.Ecommerce.repository;

import com.example.Ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByEmail(String email);
    Optional<Cart> findByUserId(Long userId);
}