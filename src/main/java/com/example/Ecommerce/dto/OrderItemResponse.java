package com.example.Ecommerce.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemResponse {

    private Long id;

    private Long productId;

    private String productName;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;
}