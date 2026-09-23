package com.example.Ecommerce.services;

import com.example.Ecommerce.dto.AuthResponse;
import com.example.Ecommerce.dto.LoginRequest;
import com.example.Ecommerce.dto.RefreshTokenResponse;
import com.example.Ecommerce.dto.RegisterRequest;
import com.example.Ecommerce.dto.UserResponse;
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
import org.springframework.transaction.annotation.Transactional;

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
            RefreshTokenService refreshTokenService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
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

        String refreshToken =
                refreshTokenService.createRefreshToken(savedUser);

        return new AuthResponse(
                accessToken,
                refreshToken,
                mapToResponse(savedUser)
        );
    }

    @Transactional
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

        User savedUser =
                userRepository.findByEmail(
                        userDetails.getUsername()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        String accessToken =
                jwtService.generateToken(userDetails);

        String refreshToken =
                refreshTokenService.createRefreshToken(savedUser);

        return new AuthResponse(
                accessToken,
                refreshToken,
                mapToResponse(savedUser)
        );
    }

    @Transactional
    public RefreshTokenResponse refreshAccessToken(
            String refreshTokenValue) {

        RefreshToken oldRefreshToken =
                refreshTokenService.findByToken(
                        refreshTokenValue
                );

        refreshTokenService.verifyExpiration(
                oldRefreshToken
        );

        User user = oldRefreshToken.getUser();

        String accessToken =
                jwtService.generateToken(
                        new CustomUserDetails(user)
                );

        String newRefreshToken =
                refreshTokenService.rotateRefreshToken(
                        oldRefreshToken
                );

        return new RefreshTokenResponse(
                accessToken,
                newRefreshToken
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