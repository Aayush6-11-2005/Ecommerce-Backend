package com.example.Ecommerce.services;

import com.example.Ecommerce.dto.OrderItemResponse;
import com.example.Ecommerce.dto.OrderResponse;
import com.example.Ecommerce.dto.OrderStatusRequest;
import com.example.Ecommerce.entity.*;
import com.example.Ecommerce.enums.OrderStatus;
import com.example.Ecommerce.repository.CartRepository;
import com.example.Ecommerce.repository.OrderRepository;
import com.example.Ecommerce.repository.ProductRepository;
import com.example.Ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    // Create order from cart
    @Transactional
    public OrderResponse createOrder(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();

        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            int quantity = cartItem.getQuantity();

            // Check stock
            if (product.getStock() < quantity) {
                throw new RuntimeException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            // Create OrderItem
            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);

            // Store current price
            orderItem.setPrice(product.getPrice());

            orderItems.add(orderItem);

            // Calculate subtotal
            BigDecimal subtotal =
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(quantity)
                            );

            totalAmount = totalAmount.add(subtotal);

            // Reduce stock
            product.setStock(product.getStock() - quantity);

            productRepository.save(product);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToResponse(savedOrder);
    }

    // Get order by ID
    public OrderResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        return mapToResponse(order);
    }

    // Get all orders of user
    public List<OrderResponse> getUserOrders(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Update order status
    public OrderResponse updateStatus(
            Long orderId,
            OrderStatusRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        order.setStatus(request.getStatus());

        Order updatedOrder = orderRepository.save(order);

        return mapToResponse(updatedOrder);
    }

    // Cancel order
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException(
                    "Delivered order cannot be cancelled"
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException(
                    "Order is already cancelled"
            );
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            product.setStock(
                    product.getStock() + item.getQuantity()
            );

            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order updatedOrder = orderRepository.save(order);

        return mapToResponse(updatedOrder);
    }

    // Entity -> DTO
    private OrderResponse mapToResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setUserId((long) order.getUser().getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setOrderDate(order.getOrderDate());

        List<OrderItemResponse> items = new ArrayList<>();

        for (OrderItem item : order.getItems()) {

            OrderItemResponse itemResponse =
                    new OrderItemResponse();

            Product product = item.getProduct();

            BigDecimal subtotal =
                    item.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.getQuantity()
                                    )
                            );

            itemResponse.setId(item.getId());
            itemResponse.setProductId(product.getId());
            itemResponse.setProductName(product.getName());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());
            itemResponse.setSubtotal(subtotal);

            items.add(itemResponse);
        }

        response.setItems(items);

        return response;
    }
}