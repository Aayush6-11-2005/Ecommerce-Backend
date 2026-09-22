package com.example.Ecommerce.controller;

import com.example.Ecommerce.dto.OrderResponse;
import com.example.Ecommerce.dto.OrderStatusRequest;
import com.example.Ecommerce.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }



    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                orderService.createOrder(
                        authentication.getName()
                )
        );
    }



    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        return ResponseEntity.ok(
                orderService.getOrder(
                        authentication.getName(),
                        orderId
                )
        );
    }



    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                orderService.getUserOrders(
                        authentication.getName()
                )
        );
    }


    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusRequest request
    ) {

        return ResponseEntity.ok(
                orderService.updateStatus(
                        orderId,
                        request
                )
        );
    }



    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        return ResponseEntity.ok(
                orderService.cancelOrder(
                        authentication.getName(),
                        orderId
                )
        );
    }
}