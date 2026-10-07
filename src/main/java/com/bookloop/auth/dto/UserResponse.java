package com.bookloop.auth.dto;

import java.time.LocalDateTime;

import com.bookloop.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String city;

    private String location;

    private String pincode;

    private String bio;

    private String avatar;

    private String role;

    private boolean verified;

    private boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .city(user.getCity())
                .location(user.getLocation())
                .pincode(user.getPincode())
                .bio(user.getBio())
                .avatar(user.getAvatar())
                .role(
                        user.getRole() != null
                                ? user.getRole().name()
                                : "USER"
                )
                .verified(user.isVerified())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}