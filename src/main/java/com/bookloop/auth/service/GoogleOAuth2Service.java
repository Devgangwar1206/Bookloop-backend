package com.bookloop.auth.service;

 
import java.security.SecureRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookloop.user.entity.Role;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User processGoogleUser(
            String email,
            String name,
            String picture) {

        User user = userRepository.findByEmail(email)
                .orElse(null);

        // Existing BookLoop user
        if (user != null) {

            if (name != null && !name.isBlank()) {
                user.setName(name);
            }

            if (picture != null && !picture.isBlank()) {
                user.setAvatar(picture);
            }

            return userRepository.save(user);
        }

        // New Google user
        User newUser = new User();

        newUser.setName(name);
        newUser.setEmail(email);

        String randomPassword =
                String.valueOf(new SecureRandom().nextLong());

        newUser.setPassword(
                passwordEncoder.encode(randomPassword)
        );

        newUser.setAvatar(picture);
        newUser.setRole(Role.USER);
        newUser.setEnabled(true);

        return userRepository.save(newUser);
    }
}