package com.bookloop.chat.dto;

 
import java.util.ArrayList;
import java.util.List;

public class ConversationDto {
    private String id;
    private String bookId;
    private String bookTitle;
    private Double bookPrice;
    private String bookImage;
    private UserSummaryDto otherUser;
    private UserSummaryDto buyer;
    private String currentUserId;
    private String lastMessage;
    private String lastMessageTime;
    private Integer unreadCount;
    private List<ChatMessageDto> messages = new ArrayList<>();

    public ConversationDto() {}

    public ConversationDto(String id, String bookId, String bookTitle, Double bookPrice, String bookImage,
                           UserSummaryDto otherUser, String lastMessage, String lastMessageTime, Integer unreadCount) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookPrice = bookPrice;
        this.bookImage = bookImage;
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
        this.messages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Double getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(Double bookPrice) {
        this.bookPrice = bookPrice;
    }

    public String getBookImage() {
        return bookImage;
    }

    public void setBookImage(String bookImage) {
        this.bookImage = bookImage;
    }

    public UserSummaryDto getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(UserSummaryDto otherUser) {
        this.otherUser = otherUser;
    }

    public UserSummaryDto getBuyer() {
        return buyer;
    }

    public void setBuyer(UserSummaryDto buyer) {
        this.buyer = buyer;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<ChatMessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDto> messages) {
        this.messages = messages;
    }
}
