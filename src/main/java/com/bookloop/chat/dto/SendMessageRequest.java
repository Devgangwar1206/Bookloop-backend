package com.bookloop.chat.dto;

 
import jakarta.validation.constraints.NotBlank;

public class SendMessageRequest {

    private String conversationId;

    private String senderId;

    @NotBlank(message = "Message text cannot be blank")
    private String text;

    private String messageType; // "TEXT", "IMAGE", "OFFER", "SYSTEM"

    public SendMessageRequest() {
        this.messageType = "TEXT";
    }

    public SendMessageRequest(String conversationId, String senderId, String text) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.text = text;
        this.messageType = "TEXT";
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

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
