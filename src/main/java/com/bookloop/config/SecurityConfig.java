package com.bookloop.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.bookloop.security.CustomUserDetailsService;
import com.bookloop.security.GoogleOAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*,https://*,http://192.168.*}")
    private List<String> allowedOrigins;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKey key = new javax.crypto.spec.SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

    	http
	        .cors(cors -> {})
	        .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.IF_REQUIRED
                )
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/forgot-password",
                    "/api/v1/auth/verify-otp",
                    "/api/v1/auth/reset-password",

                    "/oauth2/**",
                    "/login/oauth2/**"
                )
                .permitAll()
                
             // ⭐ COMMUNITY REPORTS
                .requestMatchers("/api/v1/reports", "/api/v1/reports/**")
                .permitAll()

             // ⭐ ADMIN APIs
                .requestMatchers("/api/v1/admin/**")
                .permitAll()

                .requestMatchers("/api/v1/books/my")
                .authenticated()
                
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/books",
                        "/api/v1/books/search",
                        "/api/v1/books/popular",
                        "/api/v1/books/latest",
                        "/api/v1/books/near-you",
                        "/api/v1/books/seller/**",
                        "/api/v1/books/*"
                    ).permitAll()

                // ⭐ REVIEWS & RATINGS
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/reviews",
                        "/api/v1/reviews/**"
                    ).permitAll()
                
             // ---------------------------------------------
                // HEALTH ENDPOINTS (Keep-Alive for Render & Monitoring)
                // ---------------------------------------------

                .requestMatchers(
                    "/health",
                    "/api/health",
                    "/api/v1/health",
                    "/api/chat/health"
                )
                .permitAll()

                // ---------------------------------------------
                // SOCKJS / WEBSOCKET HANDSHAKE
                // ---------------------------------------------
                //
                // Important:
                // HTTP handshake is allowed.
                // Actual JWT authentication happens on
                // STOMP CONNECT through JwtStompInterceptor.
                //

                .requestMatchers(
                    "/ws-chat",
                    "/ws-chat/**"
                )
                .permitAll()

                // ---------------------------------------------
                // CHAT REST APIs
                // ---------------------------------------------

                .requestMatchers(
                    "/api/chat/**"
                )
                .authenticated()


                .anyRequest()
                .authenticated()
            )

            .oauth2Login(oauth2 ->
                oauth2.successHandler(
                    googleOAuth2SuccessHandler
                )
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> {})
            );

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(
            (allowedOrigins != null && !allowedOrigins.isEmpty())
                ? allowedOrigins
                : List.of("http://localhost:*", "http://127.0.0.1:*", "https://*", "http://192.168.*")
        );

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type",
                "Accept"
            )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}