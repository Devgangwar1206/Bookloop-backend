package com.bookloop.book.dto;
 
import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCreateRequest {

    private String title;
    private String author;

    private BigDecimal price;
    private BigDecimal originalPrice;

    private String condition;
    private String category;

    private String listingType;
    private String transactionType;

    private String exchangePreferences;

    private boolean negotiable;

    private String location;
    private String city;

    private String language;
    private String delivery;

    private String isbn;
    private String edition;
    private String publisher;

    private Integer pages;
    private Integer publicationYear;

    private String description;

    private List<String> images;
}