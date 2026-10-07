package com.bookloop.chat.controller;

import com.bookloop.chat.dto.*;
import com.bookloop.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
 public class ChatRestController {

    private final ChatService chatService;

    @Autowired
    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    private String resolveUserId(String userId, Authentication authentication) {
        if (userId != null && !userId.isBlank() && !"u-me".equals(userId)) {
            return userId;
        }
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }
        return (userId != null && !userId.isBlank()) ? userId : "u-me";
    }

    /**
     * Health check endpoint to verify backend connectivity from frontend
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "bookloop-chat-backend");
        status.put("timestamp", System.currentTimeMillis());
        status.put("websocketEndpoint", "/ws-chat");
        return ResponseEntity.ok(status);
    }

    /**
     * Get all active conversations for the user
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getConversations(
            @RequestParam(value = "userId", required = false) String userId,
            Authentication authentication) {
        String effectiveUserId = resolveUserId(userId, authentication);
        List<ConversationDto> list = chatService.getConversations(effectiveUserId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get a specific conversation by ID (including participants and recent messages)
     */
    @GetMapping("/conversations/{chatId}")
    public ResponseEntity<ConversationDto> getConversationById(
            @PathVariable("chatId") String chatId,
            @RequestParam(value = "userId", required = false) String userId,
            Authentication authentication) {
        String effectiveUserId = resolveUserId(userId, authentication);
        ConversationDto conv = chatService.getConversationById(chatId, effectiveUserId);
        if (conv == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(conv);
    }

    /**
     * Get messages history for a conversation
     */
    @GetMapping("/conversations/{chatId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessages(@PathVariable("chatId") String chatId) {
        List<ChatMessageDto> messages = chatService.getMessages(chatId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Send a new message to a conversation via REST API
     */
    @PostMapping("/conversations/{chatId}/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable("chatId") String chatId,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        request.setConversationId(chatId);
        if (request.getSenderId() == null || request.getSenderId().isBlank() || "u-me".equals(request.getSenderId())) {
            if (authentication != null && authentication.getName() != null) {
                request.setSenderId(authentication.getName());
            }
        }
        ChatMessageDto created = chatService.sendMessage(chatId, request);
        return ResponseEntity.ok(created);
    }

    /**
     * Start a new chat or retrieve existing conversation with a book seller
     */
    @PostMapping("/start")
    public ResponseEntity<ConversationDto> startOrGetChat(
            @RequestBody StartChatRequest request,
            Authentication authentication) {
        if (request.getCurrentUserId() == null || request.getCurrentUserId().isBlank() || "u-me".equals(request.getCurrentUserId())) {
            if (authentication != null && authentication.getName() != null) {
                request.setCurrentUserId(authentication.getName());
            }
        }
        ConversationDto conv = chatService.startOrGetChatWithSeller(request);
        return ResponseEntity.ok(conv);
    }

    /**
     * Mark all messages in a conversation as read
     */
    @PostMapping("/conversations/{chatId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable("chatId") String chatId,
            @RequestParam(value = "userId", required = false) String userId,
            Authentication authentication) {
        String effectiveUserId = resolveUserId(userId, authentication);
        long remaining = chatService.markAsRead(chatId, effectiveUserId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("unreadCount", remaining);
        return ResponseEntity.ok(resp);
    }

    /**
     * Mark all conversations as read
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @RequestParam(value = "userId", required = false) String userId,
            Authentication authentication) {
        String effectiveUserId = resolveUserId(userId, authentication);
        long remaining = chatService.markAllAsRead(effectiveUserId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("unreadCount", remaining);
        return ResponseEntity.ok(resp);
    }

    /**
     * Get total unread count for badge indicators
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @RequestParam(value = "userId", required = false) String userId,
            Authentication authentication) {
        String effectiveUserId = resolveUserId(userId, authentication);
        long count = chatService.getUnreadCount(effectiveUserId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("unreadCount", count);
        return ResponseEntity.ok(resp);
    }
}
