package com.example.Ecommerce.controller;

import com.example.Ecommerce.dto.CartItemRequest;
import com.example.Ecommerce.dto.CartResponse;
import com.example.Ecommerce.services.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
    @GetMapping()
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication) {

        return ResponseEntity.ok(
                cartService.getCart(authentication.getName())
        );
    }

    // Add product
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request) {

        return ResponseEntity.ok(
                cartService.addToCart(authentication.getName(), request)
        );
    }

    // Update quantity
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            Authentication authentication,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        authentication.getName(),
                        itemId,
                        quantity
                )
        );
    }

    // Remove item
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            Authentication authentication,
            @PathVariable Long itemId) {

        return ResponseEntity.ok(
                cartService.removeFromCart(authentication.getName(), itemId)
        );
    }

    // Clear cart
    @DeleteMapping( )
    public ResponseEntity<String> clearCart(
            Authentication authentication) {

        cartService.clearCart(authentication.getName());

        return ResponseEntity.ok(
                "Cart cleared successfully"
        );
    }
}