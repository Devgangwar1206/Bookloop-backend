package com.bookloop.book.repository;
 
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.bookloop.book.entity.Book;
import com.bookloop.user.entity.User;

@Repository
public interface BookRepository
        extends JpaRepository<Book, Long>,
                JpaSpecificationExecutor<Book> {

    List<Book> findBySeller(User seller);

    List<Book> findBySellerOrderByCreatedAtDesc(User seller);

    List<Book> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<Book> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, String status);

    long countBySeller(User seller);

    long countBySellerAndStatus(User seller, String status);

    List<Book> findByStatus(String status);

    List<Book> findBySellerAndStatus(User seller, String status);
    
    List<Book> findTop10ByStatusOrderByViewsDescCreatedAtDesc(
            String status
    );

    List<Book> findTop10ByStatusOrderByCreatedAtDesc(
            String status
    );

    List<Book> findByStatusAndCityIgnoreCaseOrderByCreatedAtDesc(
            String status,
            String city
    );
}