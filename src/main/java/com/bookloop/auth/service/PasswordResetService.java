package com.bookloop.auth.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookloop.user.entity.PasswordResetToken;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.PasswordResetTokenRepository;
import com.bookloop.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
 
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Transactional
    public void sendOtp(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No account found with this email"
                        ));

        // Delete old OTPs
        passwordResetTokenRepository.deleteByEmail(email);

        // Generate 6 digit OTP
        String otp = String.format(
                "%06d",
                new Random().nextInt(1_000_000)
        );

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .email(email)
                        .otp(otp)
                        .expiresAt(
                                LocalDateTime.now().plusMinutes(10)
                        )
                        .verified(false)
                        .build();

        passwordResetTokenRepository.save(resetToken);

        // Send email
        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(user.getEmail());
        message.setSubject("BookLoop Password Reset OTP");

        message.setText(
                "Hello " + user.getName() + ",\n\n"
                + "Your BookLoop password reset OTP is: "
                + otp
                + "\n\n"
                + "This OTP is valid for 10 minutes.\n\n"
                + "If you did not request a password reset, "
                + "please ignore this email.\n\n"
                + "Regards,\n"
                + "BookLoop Team"
        );

        mailSender.send(message);
    }


    public void verifyOtp(
            String email,
            String otp) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findTopByEmailOrderByIdDesc(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "OTP not found"
                                ));

        // Check expiry
        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "OTP has expired"
            );
        }

        // Check OTP
        if (!resetToken.getOtp().equals(otp)) {

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        resetToken.setVerified(true);

        passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public void resetPassword(
            String email,
            String otp,
            String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findTopByEmailOrderByIdDesc(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "OTP not found"
                                ));

        // Check expiry
        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "OTP has expired"
            );
        }

        // Check OTP
        if (!resetToken.getOtp().equals(otp)) {

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        // OTP must be verified first
        if (!resetToken.isVerified()) {

            throw new RuntimeException(
                    "Please verify OTP first"
            );
        }

        // Update password
        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        // Delete used OTP
        passwordResetTokenRepository.deleteByEmail(email);
    }
}