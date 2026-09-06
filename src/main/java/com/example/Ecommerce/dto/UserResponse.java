package com.example.Ecommerce.dto;


import com.example.Ecommerce.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private Role role;
}