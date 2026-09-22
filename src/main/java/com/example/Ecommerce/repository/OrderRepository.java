package com.example.Ecommerce.repository;

import com.example.Ecommerce.entity.Cart;
import com.example.Ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByEmail(String email);

    List<Order> findByUserId(Long userId);
}