package com.bookloop.book.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bookloop.user.entity.User;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


     // BASIC BOOK INFORMATION
 
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;

    private String condition;

    private String category;


     // LISTING INFORMATION
 
    private String listingType;

    private String transactionType;

    @Column(columnDefinition = "TEXT")
    private String exchangePreferences;

    private boolean negotiable;


     // LOCATION & DELIVERY
 
    private String location;

    private String city;

    private String language;

    private String delivery;


     // BOOK DETAILS
 
    private String isbn;

    private String edition;

    private String publisher;

    private Integer pages;

    private Integer publicationYear;

    @Column(columnDefinition = "TEXT")
    private String description;


     // BOOK IMAGES
 
    @ElementCollection
    @CollectionTable(
        name = "book_images",
        joinColumns = @JoinColumn(name = "book_id")
    )
    
    @Column(name = "image_url", columnDefinition = "TEXT")
    @OrderColumn(name = "image_order")
    private List<String> images = new ArrayList<>();


     // LISTING STATUS & STATS
 
    private String status;

    private Long views = 0L;


     // SELLER
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;


     // TIMESTAMPS
 
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;


     // CONSTRUCTORS
 
    public Book() {
    }
}