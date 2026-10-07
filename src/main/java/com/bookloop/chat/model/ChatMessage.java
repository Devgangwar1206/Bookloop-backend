package com.bookloop.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "chat_messages",
    indexes = {
        @Index(
            name = "idx_conv_time",
            columnList = "conversation_id, created_at"
        )
    }
)
public class ChatMessage {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "conversation_id", nullable = false, length = 64)
    private String conversationId;

    @Column(name = "sender_id", nullable = false, length = 64)
    private String senderId;

    @Column(name = "text", nullable = false, length = 4000)
    private String text;

    @Column(name = "time_display", length = 64)
    private String time;

    @Column(name = "message_type", length = 32)
    private String messageType;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ChatMessage() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.messageType = "TEXT";
    }

    public ChatMessage(
            String id,
            String conversationId,
            String senderId,
            String text,
            String time,
            String messageType
    ) {
        this.id = id != null && !id.isBlank()
                ? id
                : UUID.randomUUID().toString();

        this.conversationId = conversationId;
        this.senderId = senderId;
        this.text = text;
        this.time = time;
        this.messageType =
                messageType != null ? messageType : "TEXT";

        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {

        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.messageType == null || this.messageType.isBlank()) {
            this.messageType = "TEXT";
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}