package com.bookloop.chat.model;

 
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_conv_user", columnList = "current_user_id, updated_at"),
    @Index(name = "idx_conv_book_seller", columnList = "book_id, other_user_id")
})
public class Conversation {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "book_id", length = 64)
    private String bookId;

    @Column(name = "book_title", length = 255)
    private String bookTitle;

    @Column(name = "book_price")
    private Double bookPrice;

    @Column(name = "book_image", length = 1024)
    private String bookImage;

    // Participant / Seller information
    @Column(name = "other_user_id", length = 64)
    private String otherUserId;

    @Column(name = "other_user_name", length = 128)
    private String otherUserName;

    @Column(name = "other_user_avatar", columnDefinition = "TEXT")
    private String otherUserAvatar;

    @Column(name = "other_user_verified")
    private Boolean otherUserVerified;

    @Column(name = "other_user_location", length = 255)
    private String otherUserLocation;

    @Column(name = "current_user_id", length = 64)
    private String currentUserId;

    @Column(name = "current_user_name", length = 128)
    private String currentUserName;

    @Column(name = "current_user_avatar", columnDefinition = "TEXT")
    private String currentUserAvatar;

    @Column(name = "current_user_verified")
    private Boolean currentUserVerified;

    @Column(name = "current_user_location", length = 255)
    private String currentUserLocation;

    @Column(name = "last_message", length = 4000)
    private String lastMessage;

    @Column(name = "last_message_time", length = 64)
    private String lastMessageTime;

    @Column(name = "unread_count")
    private Integer unreadCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Conversation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.unreadCount = 0;
        this.otherUserVerified = false;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.unreadCount == null) {
            this.unreadCount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserAvatar() {
        return otherUserAvatar;
    }

    public void setOtherUserAvatar(String otherUserAvatar) {
        this.otherUserAvatar = otherUserAvatar;
    }

    public Boolean getOtherUserVerified() {
        return otherUserVerified;
    }

    public void setOtherUserVerified(Boolean otherUserVerified) {
        this.otherUserVerified = otherUserVerified;
    }

    public String getOtherUserLocation() {
        return otherUserLocation;
    }

    public void setOtherUserLocation(String otherUserLocation) {
        this.otherUserLocation = otherUserLocation;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

    public String getCurrentUserAvatar() {
        return currentUserAvatar;
    }

    public void setCurrentUserAvatar(String currentUserAvatar) {
        this.currentUserAvatar = currentUserAvatar;
    }

    public Boolean getCurrentUserVerified() {
        return currentUserVerified;
    }

    public void setCurrentUserVerified(Boolean currentUserVerified) {
        this.currentUserVerified = currentUserVerified;
    }

    public String getCurrentUserLocation() {
        return currentUserLocation;
    }

    public void setCurrentUserLocation(String currentUserLocation) {
        this.currentUserLocation = currentUserLocation;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
