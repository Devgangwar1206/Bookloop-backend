package com.bookloop.review.dto;

 
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReviewResponse {

    private Long id;

    private Long bookId;

    private String bookTitle;

    private Long reviewerId;

    private String reviewerName;

    private String reviewerAvatar;

    private Long sellerId;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
}