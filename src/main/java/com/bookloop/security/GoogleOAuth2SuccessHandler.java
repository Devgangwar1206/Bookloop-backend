package com.bookloop.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.bookloop.auth.service.GoogleOAuth2Service;
import com.bookloop.user.entity.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final GoogleOAuth2Service googleOAuth2Service;

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        // Google authenticated user
        OidcUser oidcUser =
                (OidcUser) authentication.getPrincipal();

        // Get user information from Google
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getPicture();

        // Create or update BookLoop user
        User user = googleOAuth2Service.processGoogleUser(
                email,
                name,
                picture
        );

        // Load BookLoop user as Spring Security UserDetails
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        user.getEmail()
                );

        // Generate BookLoop JWT
        String token =
                jwtService.generateToken(userDetails);

        // Encode JWT before sending it to frontend
        String encodedToken =
                URLEncoder.encode(
                        token,
                        StandardCharsets.UTF_8
                );

        // Redirect user to frontend success page
        String targetBase = (frontendUrl != null && !frontendUrl.isBlank())
                ? frontendUrl.replaceAll("/+$", "")
                : "http://localhost:3000";
        response.sendRedirect(
                targetBase + "/oauth2/success?token=" + encodedToken
        );
    }
}