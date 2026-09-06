package com.example.Ecommerce.controller;

import com.example.Ecommerce.dto.OrderResponse;
import com.example.Ecommerce.dto.OrderStatusRequest;
import com.example.Ecommerce.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create order from cart
    @PostMapping("/user/{userId}")
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                orderService.createOrder(userId)
        );
    }

    // Get order
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.getOrder(orderId)
        );
    }

    // Get user's orders
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                orderService.getUserOrders(userId)
        );
    }

    // Update status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusRequest request) {

        return ResponseEntity.ok(
                orderService.updateStatus(
                        orderId,
                        request
                )
        );
    }

    // Cancel order
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.cancelOrder(orderId)
        );
    }
}