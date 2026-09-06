package com.example.Ecommerce.controller;

import com.example.Ecommerce.dto.CartItemRequest;
import com.example.Ecommerce.dto.CartResponse;
import com.example.Ecommerce.services.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Get cart
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                cartService.getCart(userId)
        );
    }

    // Add product
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable Long userId,
            @Valid @RequestBody CartItemRequest request) {

        return ResponseEntity.ok(
                cartService.addToCart(userId, request)
        );
    }

    // Update quantity
    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        userId,
                        itemId,
                        quantity
                )
        );
    }

    // Remove item
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long itemId) {

        return ResponseEntity.ok(
                cartService.removeFromCart(userId, itemId)
        );
    }

    // Clear cart
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> clearCart(
            @PathVariable Long userId) {

        cartService.clearCart(userId);

        return ResponseEntity.ok(
                "Cart cleared successfully"
        );
    }
}