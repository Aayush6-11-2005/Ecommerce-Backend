package com.example.Ecommerce.services;

import com.example.Ecommerce.dto.*;
import com.example.Ecommerce.entity.RefreshToken;
import com.example.Ecommerce.entity.User;
import com.example.Ecommerce.enums.Role;
import com.example.Ecommerce.exception.BadRequestException;
import com.example.Ecommerce.exception.ResourceNotFoundException;
import com.example.Ecommerce.repository.UserRepository;
import com.example.Ecommerce.security.CustomUserDetails;
import com.example.Ecommerce.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService ) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        String accessToken =
                jwtService.generateToken(
                        new CustomUserDetails(savedUser)
                );

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(savedUser);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                mapToResponse(savedUser)
        );
    }

    public AuthResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(
                        userDetails.getUsername()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        String accessToken =
                jwtService.generateToken(userDetails);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                mapToResponse(user)
        );
    }
    public RefreshTokenResponse refreshAccessToken(
            String refreshTokenValue) {

        RefreshToken refreshToken =
                refreshTokenService.findByToken(refreshTokenValue);

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        String accessToken =
                jwtService.generateToken(
                        new CustomUserDetails(user)
                );

        return new RefreshTokenResponse(accessToken);
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