package com.bookloop.chat.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtStompInterceptor jwtStompInterceptor;

    @Value("${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*,https://*,http://192.168.*,*}")
    private List<String> allowedOrigins;

    public WebSocketConfig(JwtStompInterceptor jwtStompInterceptor) {
        this.jwtStompInterceptor = jwtStompInterceptor;
    }

    // ---------------------------------------------------------
    // MESSAGE BROKER
    // ---------------------------------------------------------

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // In-memory message broker topics
        config.enableSimpleBroker(
            "/topic",
            "/queue"
        );

        // Prefix for messages routed to @MessageMapping controllers
        config.setApplicationDestinationPrefixes(
            "/app"
        );

        // Prefix for user-targeted queues
        config.setUserDestinationPrefix(
            "/user"
        );
    }

    // ---------------------------------------------------------
    // JWT STOMP INTERCEPTOR
    // ---------------------------------------------------------

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
            jwtStompInterceptor
        );
    }

    // ---------------------------------------------------------
    // STOMP ENDPOINTS
    // ---------------------------------------------------------

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] patterns = (allowedOrigins != null && !allowedOrigins.isEmpty())
                ? allowedOrigins.toArray(new String[0])
                : new String[]{"http://localhost:*", "http://127.0.0.1:*", "*"};

        // SockJS fallback endpoint (used by sockjs-client in frontend)
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns(patterns)
                .withSockJS();

        // Native WebSocket endpoint (for pure WebSocket connection without SockJS wrapper)
        registry.addEndpoint("/ws-chat-raw")
                .setAllowedOriginPatterns(patterns);
    }
}