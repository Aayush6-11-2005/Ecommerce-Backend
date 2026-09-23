package com.example.Ecommerce.services;

import com.example.Ecommerce.dto.OrderCreatedEvent;
import com.example.Ecommerce.dto.OrderItemResponse;
import com.example.Ecommerce.dto.OrderResponse;
import com.example.Ecommerce.dto.OrderStatusRequest;
import com.example.Ecommerce.entity.Cart;
import com.example.Ecommerce.entity.CartItem;
import com.example.Ecommerce.entity.Order;
import com.example.Ecommerce.entity.OrderItem;
import com.example.Ecommerce.entity.Product;
import com.example.Ecommerce.entity.User;
import com.example.Ecommerce.enums.OrderStatus;
import com.example.Ecommerce.exception.BadRequestException;
import com.example.Ecommerce.exception.ResourceNotFoundException;
import com.example.Ecommerce.kafka.KafkaProducerService;
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
    private final KafkaProducerService kafkaProducerService;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            KafkaProducerService kafkaProducerService) {

        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public OrderResponse createOrder(String email) {

        User user = getUser(email);

        Cart cart =
                cartRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart not found"
                                )
                        );

        if (cart.getItems().isEmpty()) {

            throw new BadRequestException(
                    "Cart is empty"
            );
        }

        Order order = new Order();

        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems =
                new ArrayList<>();

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            Product product =
                    cartItem.getProduct();

            int quantity =
                    cartItem.getQuantity();

            if (product.getStock() < quantity) {

                throw new BadRequestException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(product.getPrice());

            orderItems.add(orderItem);

            BigDecimal subtotal =
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            quantity
                                    )
                            );

            totalAmount =
                    totalAmount.add(subtotal);

            product.setStock(
                    product.getStock() - quantity
            );

            productRepository.save(product);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder =
                orderRepository.save(order);

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        savedOrder.getId(),
                        user.getId(),
                        savedOrder.getTotalAmount()
                );

        kafkaProducerService
                .sendOrderCreatedEvent(event);

        cart.getItems().clear();

        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrder(
            String email,
            Long orderId) {

        User user = getUser(email);

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        if (!order.getUser()
                .getId()
                .equals(user.getId())) {

            throw new BadRequestException(
                    "You cannot access this order"
            );
        }

        return mapToResponse(order);
    }

    public List<OrderResponse> getUserOrders(
            String email) {

        User user = getUser(email);

        return orderRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(
            Long orderId,
            OrderStatusRequest request) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        OrderStatus current =
                order.getStatus();

        OrderStatus newStatus =
                request.getStatus();

        if (current == OrderStatus.CANCELLED) {

            throw new BadRequestException(
                    "Cancelled order cannot be updated"
            );
        }

        if (current == OrderStatus.DELIVERED) {

            throw new BadRequestException(
                    "Delivered order cannot be updated"
            );
        }

        if (newStatus == OrderStatus.CANCELLED) {

            throw new BadRequestException(
                    "Use the cancel order endpoint"
            );
        }

        order.setStatus(newStatus);

        return mapToResponse(
                orderRepository.save(order)
        );
    }

    @Transactional
    public OrderResponse cancelOrder(
            String email,
            Long orderId) {

        User user = getUser(email);

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        if (!order.getUser()
                .getId()
                .equals(user.getId())) {

            throw new BadRequestException(
                    "You cannot cancel this order"
            );
        }

        if (order.getStatus()
                == OrderStatus.DELIVERED) {

            throw new BadRequestException(
                    "Delivered order cannot be cancelled"
            );
        }

        if (order.getStatus()
                == OrderStatus.CANCELLED) {

            throw new BadRequestException(
                    "Order is already cancelled"
            );
        }

        for (OrderItem item : order.getItems()) {

            Product product =
                    item.getProduct();

            product.setStock(
                    product.getStock()
                            + item.getQuantity()
            );

            productRepository.save(product);
        }

        order.setStatus(
                OrderStatus.CANCELLED
        );

        return mapToResponse(
                orderRepository.save(order)
        );
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private OrderResponse mapToResponse(
            Order order) {

        OrderResponse response =
                new OrderResponse();

        response.setId(order.getId());
        response.setUserId(
                order.getUser().getId()
        );
        response.setTotalAmount(
                order.getTotalAmount()
        );
        response.setStatus(
                order.getStatus()
        );
        response.setOrderDate(
                order.getOrderDate()
        );

        List<OrderItemResponse> items =
                new ArrayList<>();

        for (OrderItem item :
                order.getItems()) {

            Product product =
                    item.getProduct();

            BigDecimal subtotal =
                    item.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.getQuantity()
                                    )
                            );

            OrderItemResponse itemResponse =
                    new OrderItemResponse();

            itemResponse.setId(item.getId());
            itemResponse.setProductId(
                    product.getId()
            );
            itemResponse.setProductName(
                    product.getName()
            );
            itemResponse.setQuantity(
                    item.getQuantity()
            );
            itemResponse.setPrice(
                    item.getPrice()
            );
            itemResponse.setSubtotal(
                    subtotal
            );

            items.add(itemResponse);
        }

        response.setItems(items);

        return response;
    }
}