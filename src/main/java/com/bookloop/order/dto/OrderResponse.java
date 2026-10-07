package com.bookloop.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponse {

    private Long id;

    private String orderNumber;

    private Long bookId;

    private String bookTitle;

    private String bookAuthor;

    private String bookImage;

    private BigDecimal originalPrice;

    private BigDecimal amount;

    private Long buyerId;

    private String buyerName;

    private String buyerAvatar;

    private Long sellerId;

    private String sellerName;

    private String sellerAvatar;

    private String deliveryAddress;

    private String status;

    private String trackingNumber;

    private String condition;

    private String category;

    private String deliveryMethod;

    private String estimatedDelivery;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}