package com.example.Ecommerce.services;

import com.example.Ecommerce.dto.CartItemRequest;
import com.example.Ecommerce.dto.CartItemResponse;
import com.example.Ecommerce.dto.CartResponse;
import com.example.Ecommerce.entity.Cart;
import com.example.Ecommerce.entity.CartItem;
import com.example.Ecommerce.entity.Product;
import com.example.Ecommerce.entity.User;
import com.example.Ecommerce.exception.BadRequestException;
import com.example.Ecommerce.exception.ResourceNotFoundException;
import com.example.Ecommerce.repository.CartItemRepository;
import com.example.Ecommerce.repository.CartRepository;
import com.example.Ecommerce.repository.ProductRepository;
import com.example.Ecommerce.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "carts", key = "#email")
    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {

        User user = getUser(email);

        Cart cart = cartRepository
                .findByUserId(user.getId())
                .orElseGet(() -> createCart(user));

        return mapToResponse(cart);
    }

    @CachePut(value = "carts", key = "#email")
    @Transactional
    public CartResponse addToCart(
            String email,
            CartItemRequest request) {

        User user = getUser(email);

        Product product =
                productRepository.findById(
                        request.getProductId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: "
                                        + request.getProductId()
                        )
                );

        if (request.getQuantity() == null
                || request.getQuantity() <= 0) {

            throw new BadRequestException(
                    "Quantity must be greater than 0"
            );
        }

        Cart cart = cartRepository
                .findByUserId(user.getId())
                .orElseGet(() -> createCart(user));

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElse(null);

        int newQuantity =
                request.getQuantity();

        if (cartItem != null) {

            newQuantity =
                    cartItem.getQuantity()
                            + request.getQuantity();
        }

        if (newQuantity > product.getStock()) {

            throw new BadRequestException(
                    "Insufficient stock"
            );
        }

        if (cartItem == null) {

            cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setProduct(product);
        }

        cartItem.setQuantity(newQuantity);

        cartItemRepository.save(cartItem);

        cart.getItems().clear();
        cart.getItems().addAll(
                cartItemRepository.findByCartId(cart.getId())
        );

        return mapToResponse(cart);
    }

    @CachePut(value = "carts", key = "#email")
    @Transactional
    public CartResponse updateCartItem(
            String email,
            Long itemId,
            Integer quantity) {

        User user = getUser(email);

        Cart cart =
                cartRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart not found"
                                )
                        );

        CartItem cartItem =
                cartItemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart item not found"
                                )
                        );

        validateCartItemOwner(cart, cartItem);

        if (quantity == null || quantity <= 0) {

            throw new BadRequestException(
                    "Quantity must be greater than 0"
            );
        }

        Product product = cartItem.getProduct();

        if (quantity > product.getStock()) {

            throw new BadRequestException(
                    "Insufficient stock"
            );
        }

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        cart.getItems().clear();
        cart.getItems().addAll(
                cartItemRepository.findByCartId(cart.getId())
        );

        return mapToResponse(cart);
    }

    @CachePut(value = "carts", key = "#email")
    @Transactional
    public CartResponse removeFromCart(
            String email,
            Long itemId) {

        User user = getUser(email);

        Cart cart =
                cartRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart not found"
                                )
                        );

        CartItem cartItem =
                cartItemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart item not found"
                                )
                        );

        validateCartItemOwner(cart, cartItem);

        cartItemRepository.delete(cartItem);

        cart.getItems().removeIf(
                item -> item.getId().equals(itemId)
        );

        return mapToResponse(cart);
    }

    @CacheEvict(value = "carts", key = "#email")
    @Transactional
    public void clearCart(String email) {

        User user = getUser(email);

        Cart cart =
                cartRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart not found"
                                )
                        );

        cartItemRepository.deleteAll(cart.getItems());

        cart.getItems().clear();
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private void validateCartItemOwner(
            Cart cart,
            CartItem cartItem) {

        if (!cartItem.getCart()
                .getId()
                .equals(cart.getId())) {

            throw new BadRequestException(
                    "Cart item does not belong to this user"
            );
        }
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
        response.setUserId(cart.getUser().getId());

        List<CartItemResponse> items =
                new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {

            Product product = item.getProduct();

            BigDecimal subtotal =
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.getQuantity()
                                    )
                            );

            CartItemResponse itemResponse =
                    new CartItemResponse();

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