package com.bookloop.favorite.controller;
 
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookloop.book.dto.BookResponse;
import com.bookloop.favorite.service.FavoriteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;


    @PostMapping("/books/{bookId}/favorite")
    public ResponseEntity<Map<String, Object>> addFavorite(
            @PathVariable Long bookId,
            Authentication authentication) {

        favoriteService.addFavorite(
                bookId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                    "favorited", true,
                    "message", "Book added to favorites"
                )
        );
    }


    @DeleteMapping("/books/{bookId}/favorite")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable Long bookId,
            Authentication authentication) {

        favoriteService.removeFavorite(
                bookId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                    "favorited", false,
                    "message", "Book removed from favorites"
                )
        );
    }


    @GetMapping("/users/me/favorites")
    public ResponseEntity<List<BookResponse>>
            getMyFavorites(
                Authentication authentication) {

        return ResponseEntity.ok(
            favoriteService.getMyFavorites(
                authentication.getName()
            )
        );
    }


    @GetMapping("/books/{bookId}/favorite")
    public ResponseEntity<Boolean> isFavorite(
            @PathVariable Long bookId,
            Authentication authentication) {

        return ResponseEntity.ok(
            favoriteService.isFavorite(
                bookId,
                authentication.getName()
            )
        );
    }


    @GetMapping("/books/{bookId}/favorites/count")
    public ResponseEntity<Long> getFavoriteCount(
            @PathVariable Long bookId) {

        return ResponseEntity.ok(
            favoriteService.getFavoriteCount(bookId)
        );
    }
}