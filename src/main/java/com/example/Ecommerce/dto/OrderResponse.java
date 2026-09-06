package com.example.Ecommerce.dto;

import com.example.Ecommerce.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {

    private Long id;

    private Long userId;

    private BigDecimal totalAmount;

    private OrderStatus status;

    private LocalDateTime orderDate;

    private List<OrderItemResponse> items;
}