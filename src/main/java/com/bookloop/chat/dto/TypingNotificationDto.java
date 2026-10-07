package com.bookloop.chat.dto;

 
public class TypingNotificationDto {
    private String conversationId;
    private String userId;
    private String userName;
    private boolean typing;

    public TypingNotificationDto() {}

    public TypingNotificationDto(String conversationId, String userId, String userName, boolean typing) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.userName = userName;
        this.typing = typing;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }
}
