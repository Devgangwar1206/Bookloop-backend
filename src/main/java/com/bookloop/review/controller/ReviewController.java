package com.bookloop.review.controller;
 
import com.bookloop.review.dto.ReviewRequest;
import com.bookloop.review.dto.ReviewResponse;
import com.bookloop.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication
    ) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReviewResponse response =
                reviewService.createReview(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewResponse>> getBookReviews(
            @PathVariable Long bookId
    ) {

        return ResponseEntity.ok(
                reviewService.getBookReviews(bookId)
        );
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ReviewResponse>> getSellerReviews(
            @PathVariable Long sellerId
    ) {

        return ResponseEntity.ok(
                reviewService.getSellerReviews(sellerId)
        );
    }

    @GetMapping("/seller/{sellerId}/rating")
    public ResponseEntity<Double> getSellerRating(
            @PathVariable Long sellerId
    ) {

        return ResponseEntity.ok(
                reviewService.getSellerRating(sellerId)
        );
    }
}