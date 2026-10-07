package com.bookloop.review.service;
 
import com.bookloop.book.entity.Book;
import com.bookloop.book.repository.BookRepository;
import com.bookloop.review.dto.ReviewRequest;
import com.bookloop.review.dto.ReviewResponse;
import com.bookloop.review.entity.Review;
import com.bookloop.review.repository.ReviewRepository;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(
            ReviewRequest request,
            String reviewerEmail
    ) {

        User reviewer = userRepository
                .findByEmail(reviewerEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Book book = null;
        if (request.getBookId() != null) {
            book = bookRepository.findById(request.getBookId()).orElse(null);
        }

        User seller = null;
        if (request.getSellerId() != null) {
            seller = userRepository.findById(request.getSellerId()).orElse(null);
        } else if (book != null) {
            seller = book.getSeller();
        }

        if (seller == null) {
            throw new RuntimeException("Seller not found");
        }

        if (reviewer.getId().equals(seller.getId())) {
            throw new RuntimeException(
                    "You cannot rate yourself"
            );
        }

        // Check if an existing review exists between reviewer and seller or book
        Review review = null;
        if (book != null) {
            review = reviewRepository.findByReviewerAndBook(reviewer, book).orElse(null);
        }
        if (review == null) {
            review = reviewRepository.findFirstByReviewerAndSeller(reviewer, seller).orElse(null);
        }

        if (review != null) {
            review.setRating(request.getRating());
            if (request.getComment() != null) {
                review.setComment(request.getComment());
            }
            if (book != null) {
                review.setBook(book);
            }
            review.setCreatedAt(java.time.LocalDateTime.now());
        } else {
            review = Review.builder()
                    .reviewer(reviewer)
                    .book(book)
                    .seller(seller)
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
        }

        Review saved = reviewRepository.save(review);

        return mapToResponse(saved);
    }

    public List<ReviewResponse> getBookReviews(Long bookId) {

        Book book = bookRepository
                .findById(bookId)
                .orElseThrow(() ->
                        new RuntimeException("Book not found")
                );

        return reviewRepository
                .findByBookOrderByCreatedAtDesc(book)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ReviewResponse> getSellerReviews(Long sellerId) {

        User seller = userRepository
                .findById(sellerId)
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        return reviewRepository
                .findBySellerOrderByCreatedAtDesc(seller)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Double getSellerRating(Long sellerId) {

        User seller = userRepository
                .findById(sellerId)
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        Double rating =
                reviewRepository.getAverageRating(seller);

        return rating == null ? 0.0 : rating;
    }

    public Long getSellerReviewCount(Long sellerId) {

        User seller = userRepository
                .findById(sellerId)
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        return reviewRepository.countBySeller(seller);
    }

    private ReviewResponse mapToResponse(Review review) {

        return ReviewResponse.builder()
                .id(review.getId())
                .bookId(review.getBook() != null ? review.getBook().getId() : null)
                .bookTitle(review.getBook() != null ? review.getBook().getTitle() : null)
                .reviewerId(review.getReviewer().getId())
                .reviewerName(review.getReviewer().getName())
                .reviewerAvatar(review.getReviewer().getAvatar())
                .sellerId(review.getSeller().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}