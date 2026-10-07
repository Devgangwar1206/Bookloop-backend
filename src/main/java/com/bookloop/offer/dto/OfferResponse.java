package com.bookloop.offer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferResponse {

    private Long id;

    // Frontend expects nested buyer object
    private BuyerInfo buyer;

    // Frontend expects nested book object
    private BookInfo book;

    // Frontend expects offerPrice
    private BigDecimal offerPrice;

    private String message;

    // Frontend expects date
    private String date;

    private String status;

    private BigDecimal counterPrice;

    private Long parentOfferId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    public static class BuyerInfo {

        private Long id;
        private String name;
        private String avatar;
        private String location;
    }

    @Getter
    @Setter
    public static class BookInfo {

        private Long id;
        private String title;
        private BigDecimal originalPrice;
        private String image;
    }
}