package com.example.Ecommerce.services;

import com.example.Ecommerce.dto.AuthResponse;
import com.example.Ecommerce.dto.LoginRequest;
import com.example.Ecommerce.dto.RegisterRequest;
import com.example.Ecommerce.dto.UserResponse;
import com.example.Ecommerce.entity.User;
import com.example.Ecommerce.enums.Role;
import com.example.Ecommerce.repository.UserRepository;
import com.example.Ecommerce.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Never store plain password
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Don't allow user to register as ADMIN
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                new com.example.Ecommerce.security.CustomUserDetails(savedUser)
        );

        return new AuthResponse(
                token,
                mapToResponse(savedUser)
        );
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String token = jwtService.generateToken(
                new com.example.Ecommerce.security.CustomUserDetails(user)
        );

        return new AuthResponse(
                token,
                mapToResponse(user)
        );
    }

    private UserResponse mapToResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }
}