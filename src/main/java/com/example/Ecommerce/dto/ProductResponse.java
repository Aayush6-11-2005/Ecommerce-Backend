package com.example.Ecommerce.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponse {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private String imageUrl;

    private Long categoryId;

    private String categoryName;
}
