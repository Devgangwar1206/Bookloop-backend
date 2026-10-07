package com.bookloop.review.repository;
 
import com.bookloop.book.entity.Book;
import com.bookloop.review.entity.Review;
import com.bookloop.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    List<Review> findBySellerOrderByCreatedAtDesc(User seller);

    List<Review> findByBookOrderByCreatedAtDesc(Book book);

    long countBySeller(User seller);

    long countByBook(Book book);

    boolean existsByReviewerAndBook(
            User reviewer,
            Book book
    );

    Optional<Review> findByReviewerAndBook(
            User reviewer,
            Book book
    );

    boolean existsByReviewerAndSeller(
            User reviewer,
            User seller
    );

    Optional<Review> findFirstByReviewerAndSeller(
            User reviewer,
            User seller
    );

    @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        WHERE r.seller = :seller
    """)
    Double getAverageRating(
            @Param("seller") User seller
    );
}