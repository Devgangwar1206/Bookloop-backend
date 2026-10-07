package com.bookloop.book.dto;
 
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class SellerSummaryResponse {

    private Long id;

    private String name;

    private String email;

    private String avatar;

    private String role;

    private boolean verified;

    private boolean isBusiness;

    private Double rating;

    private Long reviewsCount;

    private String memberSince;

    private Long totalListings;

    private Long soldCount;

    private String responseRate;

    private String location;
    
    public void setBusiness(boolean business) {
        this.isBusiness = business;
    }
}