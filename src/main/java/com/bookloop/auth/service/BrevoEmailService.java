package com.bookloop.auth.service;

  
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BrevoEmailService {

    private final RestClient restClient;
    private final String senderEmail;
    private final String senderName;

    public BrevoEmailService(
            @Value("${brevo.api.url}") String apiUrl,
            @Value("${brevo.api.key}") String apiKey,
            @Value("${brevo.sender.email}") String senderEmail,
            @Value("${brevo.sender.name:BookLoop Team}") String senderName) {

        this.senderEmail = senderEmail;
        this.senderName = senderName;

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("api-key", apiKey)
                .defaultHeader("accept", "application/json")
                .build();
    }

    public void sendOtpEmail(
            String recipientEmail,
            String recipientName,
            String otp) {

        String text = "Hello " + recipientName + ",\n\n"
                + "Your BookLoop password reset OTP is: " + otp
                + "\n\nThis OTP is valid for 10 minutes."
                + "\n\nIf you did not request a password reset, "
                + "please ignore this email."
                + "\n\nRegards,\nBookLoop Team";

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", senderName,
                        "email", senderEmail),
                "to", List.of(Map.of(
                        "email", recipientEmail,
                        "name", recipientName)),
                "subject", "BookLoop Password Reset OTP",
                "textContent", text);

        restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
