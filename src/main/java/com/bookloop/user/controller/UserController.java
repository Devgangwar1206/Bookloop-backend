package com.bookloop.user.controller;
 
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bookloop.auth.dto.UserResponse;
import com.bookloop.user.dto.UpdateProfileRequest;
import com.bookloop.user.dto.UpdateSettingsRequest;
import com.bookloop.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                userService.getCurrentUser(
                        authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(
                userService.updateProfile(
                        authentication.getName(),
                        request));
    }

    @PutMapping("/me/settings")
    public ResponseEntity<UserResponse> updateSettings(
            Authentication authentication,
            @RequestBody UpdateSettingsRequest request) {

        return ResponseEntity.ok(
                userService.updateSettings(
                        authentication.getName(),
                        request));
    }
}