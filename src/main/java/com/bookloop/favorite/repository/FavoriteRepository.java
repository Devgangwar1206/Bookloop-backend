package com.bookloop.favorite.repository;
 
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookloop.book.entity.Book;
import com.bookloop.favorite.entity.Favorite;
import com.bookloop.user.entity.User;

@Repository
public interface FavoriteRepository
        extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndBook(
            User user,
            Book book
    );

    List<Favorite> findByUser(User user);

    long countByBook(Book book);

    boolean existsByUserAndBook(
            User user,
            Book book
    );

    void deleteByUserAndBook(
            User user,
            Book book
    );
}