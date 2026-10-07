package com.bookloop.auth.service;
 
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bookloop.auth.dto.AuthResponse;
import com.bookloop.auth.dto.LoginRequest;
import com.bookloop.auth.dto.RegisterRequest;
import com.bookloop.auth.dto.UserResponse;
import com.bookloop.security.JwtService;
import com.bookloop.user.entity.Role;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {

            throw new RuntimeException(
                    "Email already registered");
        }
        
        Role userRole = Role.USER;

        if ("SELLER".equalsIgnoreCase(request.getRole())) {
            userRole = Role.SELLER;
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()))
                .phone(request.getPhone())
                .city(request.getCity())
                .role(userRole)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    public AuthResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String token =
                jwtService.generateToken(userDetails);

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow();

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(UserResponse.from(user))
                .build();
    }
    
    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return UserResponse.from(user);
    }
}