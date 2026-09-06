package com.example.Ecommerce.services;

import com.example.Ecommerce.dto.CartItemRequest;
import com.example.Ecommerce.dto.CartItemResponse;
import com.example.Ecommerce.dto.CartResponse;
import com.example.Ecommerce.entity.Cart;
import com.example.Ecommerce.entity.CartItem;
import com.example.Ecommerce.entity.Product;
import com.example.Ecommerce.entity.User;
import com.example.Ecommerce.repository.CartItemRepository;
import com.example.Ecommerce.repository.CartRepository;
import com.example.Ecommerce.repository.ProductRepository;
import com.example.Ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

     public CartResponse getCart(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(user));

        return mapToResponse(cart);
    }

     public CartResponse addToCart(Long userId,
                                  CartItemRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(user));

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                )
                .orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > product.getStock()) {
                throw new RuntimeException("Insufficient stock");
            }

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
        }

        cartItemRepository.save(cartItem);

        return mapToResponse(cart);
    }

    // Update cart item quantity
    public CartResponse updateCartItem(Long userId,
                                       Long itemId,
                                       Integer quantity) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to this user");
        }

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        Product product = cartItem.getProduct();

        if (quantity > product.getStock()) {
            throw new RuntimeException("Insufficient stock");
        }

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        return mapToResponse(cart);
    }

    // Remove item from cart
    public CartResponse removeFromCart(Long userId,
                                       Long itemId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to this user");
        }

        cartItemRepository.delete(cartItem);

        return mapToResponse(cart);
    }

    // Clear complete cart
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found"));

        cart.getItems().clear();

        cartRepository.save(cart);
    }

     private Cart createCart(User user) {

        Cart cart = new Cart();

        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        return cartRepository.save(cart);
    }

      private CartResponse mapToResponse(Cart cart) {

        CartResponse response = new CartResponse();

        response.setId(cart.getId());
        response.setUserId((long) cart.getUser().getId());

        List<CartItemResponse> items = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {

            CartItemResponse itemResponse = new CartItemResponse();

            Product product = item.getProduct();

            BigDecimal subtotal =
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(item.getQuantity())
                            );

            itemResponse.setId(item.getId());
            itemResponse.setProductId(product.getId());
            itemResponse.setProductName(product.getName());
            itemResponse.setPrice(product.getPrice());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setSubtotal(subtotal);

            items.add(itemResponse);

            total = total.add(subtotal);
        }

        response.setItems(items);
        response.setTotalAmount(total);

        return response;
    }
}