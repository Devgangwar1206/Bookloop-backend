package com.bookloop.chat.dto;

 
public class StartChatRequest {
    private String bookId;
    private String bookTitle;
    private Double bookPrice;
    private String bookImage;
    private UserSummaryDto seller;
    private UserSummaryDto buyer;
    private String currentUserId;
    private String initialMessage;

    public StartChatRequest() {}

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

    public UserSummaryDto getSeller() {
        return seller;
    }

    public void setSeller(UserSummaryDto seller) {
        this.seller = seller;
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

    public String getInitialMessage() {
        return initialMessage;
    }

    public void setInitialMessage(String initialMessage) {
        this.initialMessage = initialMessage;
    }
}
