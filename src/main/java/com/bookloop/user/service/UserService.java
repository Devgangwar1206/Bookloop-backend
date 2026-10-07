package com.bookloop.user.service;

import org.springframework.stereotype.Service;

import com.bookloop.auth.dto.UserResponse;
import com.bookloop.user.dto.UpdateProfileRequest;
import com.bookloop.user.dto.UpdateSettingsRequest;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser(String email) {

        return UserResponse.from(getUser(email));
    }

    public UserResponse updateProfile(
            String email,
            UpdateProfileRequest request) {

        User user = getUser(email);

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }

        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }

        if (request.getPincode() != null) {
            user.setPincode(request.getPincode());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        return UserResponse.from(
                userRepository.save(user)
        );
    }

    public UserResponse updateSettings(
            String email,
            UpdateSettingsRequest request) {

        User user = getUser(email);

        user.setEmailNotifications(
                request.isEmailNotifications()
        );

        user.setOfferNotifications(
                request.isOfferNotifications()
        );

        user.setChatNotifications(
                request.isChatNotifications()
        );

        return UserResponse.from(
                userRepository.save(user)
        );
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }
}