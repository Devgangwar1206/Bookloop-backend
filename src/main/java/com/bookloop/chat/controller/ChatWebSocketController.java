package com.bookloop.chat.controller;

import com.bookloop.chat.dto.SendMessageRequest;
import com.bookloop.chat.dto.TypingNotificationDto;
import com.bookloop.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles incoming messages sent via STOMP: /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void handleSendMessage(@Payload SendMessageRequest request, Principal principal) {
        try {
            if (request == null || request.getConversationId() == null || request.getText() == null) {
                log.warn("Invalid message payload received: {}", request);
                return;
            }

            // Secure sender ID using authenticated STOMP principal if available
            if (principal != null && (request.getSenderId() == null || request.getSenderId().isBlank() || "u-me".equals(request.getSenderId()))) {
                request.setSenderId(principal.getName());
            }

            log.info("Received STOMP message for conversation {}: {}", request.getConversationId(), request.getText());
            chatService.sendMessage(request.getConversationId(), request);
        } catch (Exception e) {
            log.error("Failed to process STOMP message", e);
        }
    }

    /**
     * Handles typing notifications sent via STOMP: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTypingNotification(@Payload TypingNotificationDto payload, Principal principal) {
        try {
            if (payload == null || payload.getConversationId() == null) {
                return;
            }

            if (principal != null && (payload.getUserId() == null || payload.getUserId().isBlank() || "u-me".equals(payload.getUserId()))) {
                payload.setUserId(principal.getName());
            }

            // Broadcast typing event to conversation participants
            messagingTemplate.convertAndSend("/topic/chat/" + payload.getConversationId() + "/typing", payload);
        } catch (Exception e) {
            log.error("Failed to broadcast typing notification", e);
        }
    }
}
