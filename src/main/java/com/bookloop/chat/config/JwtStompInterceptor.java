package com.bookloop.chat.config;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.bookloop.security.JwtService;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class JwtStompInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtStompInterceptor.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtStompInterceptor(
            JwtService jwtService,
            UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        // Obtain the mutable accessor already linked to this message in the STOMP channel
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            accessor = StompHeaderAccessor.wrap(message);
        }

        // -----------------------------------------------------
        // Authenticate during STOMP CONNECT frame
        // -----------------------------------------------------
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (authorization == null || authorization.isBlank()) {
                authorization = accessor.getFirstNativeHeader("authorization");
            }

            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7).trim();
                if (!token.isBlank()) {
                    try {
                        String username = jwtService.extractUsername(token);
                        if (username != null && !username.isBlank()) {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            if (jwtService.isTokenValid(token, userDetails)) {
                                Principal principal = new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                                accessor.setUser(principal);
                                log.info("✅ STOMP authenticated user: {}", username);
                            } else {
                                log.warn("❌ STOMP token invalid or expired for user: {}", username);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("❌ STOMP JWT authentication failed: {}", e.getMessage());
                    }
                }
            } else {
                log.debug("STOMP CONNECT frame received without Authorization Bearer header");
            }
        }

        return message;
    }
}