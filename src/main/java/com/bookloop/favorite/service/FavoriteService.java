package com.bookloop.favorite.service;
 
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookloop.book.dto.BookResponse;
import com.bookloop.book.entity.Book;
import com.bookloop.book.service.BookService;
import com.bookloop.book.repository.BookRepository;
import com.bookloop.favorite.entity.Favorite;
import com.bookloop.favorite.repository.FavoriteRepository;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookService bookService;


    @Transactional
    public void addFavorite(
            Long bookId,
            String email) {

        User user = getUser(email);

        Book book = getBook(bookId);

        boolean exists =
                favoriteRepository.existsByUserAndBook(
                    user,
                    book
                );

        if (exists) {
            return;
        }

        Favorite favorite =
                new Favorite();

        favorite.setUser(user);
        favorite.setBook(book);
        favorite.setCreatedAt(
                LocalDateTime.now()
        );

        favoriteRepository.save(favorite);
    }


    @Transactional
    public void removeFavorite(
            Long bookId,
            String email) {

        User user = getUser(email);

        Book book = getBook(bookId);

        favoriteRepository.deleteByUserAndBook(
                user,
                book
        );
    }


    @Transactional(readOnly = true)
    public List<BookResponse> getMyFavorites(
            String email) {

        User user = getUser(email);

        return favoriteRepository
                .findByUser(user)
                .stream()
                .map(Favorite::getBook)
                .map(bookService::convertToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public boolean isFavorite(
            Long bookId,
            String email) {

        User user = getUser(email);

        Book book = getBook(bookId);

        return favoriteRepository.existsByUserAndBook(
                user,
                book
        );
    }


    @Transactional(readOnly = true)
    public long getFavoriteCount(
            Long bookId) {

        Book book = getBook(bookId);

        return favoriteRepository.countByBook(book);
    }


    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new RuntimeException(
                        "User not found"
                    )
                );
    }


    private Book getBook(Long id) {

        return bookRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Book not found"
                    )
                );
    }
}