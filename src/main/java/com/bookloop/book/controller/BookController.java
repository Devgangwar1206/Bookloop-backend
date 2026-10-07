package com.bookloop.book.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookloop.book.dto.BookCreateRequest;
import com.bookloop.book.dto.BookListResponse;
import com.bookloop.book.dto.BookResponse;
import com.bookloop.book.dto.BookStatusRequest;
import com.bookloop.book.dto.BookUpdateRequest;
import com.bookloop.book.dto.SellerSummaryResponse;
import com.bookloop.book.service.BookService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;


    // =========================
    // CREATE
    // =========================

    @PostMapping
    public ResponseEntity<BookResponse> createBook(

            @RequestBody BookCreateRequest request,

            Authentication authentication) {

        return ResponseEntity.ok(

                bookService.createBook(
                        request,
                        authentication.getName()
                )
        );
    }


    // =========================
    // MARKETPLACE
    // =========================

    @GetMapping
    public ResponseEntity<BookListResponse> getBooks() {

        List<BookResponse> books =
                bookService.getAllBooks();

        return ResponseEntity.ok(

                new BookListResponse(
                        books,
                        books.size()
                )
        );
    }


    // =========================
    // POPULAR BOOKS
    // =========================

    @GetMapping("/popular")
    public ResponseEntity<List<BookResponse>>
            getPopularBooks() {

        return ResponseEntity.ok(
                bookService.getPopularBooks()
        );
    }


    // =========================
    // LATEST BOOKS
    // =========================

    @GetMapping("/latest")
    public ResponseEntity<List<BookResponse>>
            getLatestBooks() {

        return ResponseEntity.ok(
                bookService.getLatestBooks()
        );
    }


    // =========================
    // BOOKS NEAR YOU
    // =========================

    @GetMapping("/near-you")
    public ResponseEntity<List<BookResponse>>
            getBooksNearYou(

                    @RequestParam String city) {

        return ResponseEntity.ok(

                bookService.getBooksNearYou(
                        city
                )
        );
    }


    // =========================
    // SEARCH + FILTER
    // =========================

    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>>
            searchBooks(

                    @RequestParam(
                            required = false
                    )
                    String keyword,

                    @RequestParam(
                            required = false
                    )
                    String category,

                    @RequestParam(
                            required = false
                    )
                    String condition,

                    @RequestParam(
                            required = false
                    )
                    BigDecimal minPrice,

                    @RequestParam(
                            required = false
                    )
                    BigDecimal maxPrice,

                    @RequestParam(
                            required = false
                    )
                    String city,

                    @RequestParam(
                            required = false
                    )
                    String transactionType,

                    @RequestParam(
                            required = false
                    )
                    String language,

                    @RequestParam(
                            required = false
                    )
                    String delivery,

                    @RequestParam(
                            required = false
                    )
                    String listingType,

                    @RequestParam(
                            required = false
                    )
                    Boolean verifiedOnly,

                    @RequestParam(
                            required = false
                    )
                    String sort) {

        return ResponseEntity.ok(

                bookService.searchBooks(

                        keyword,

                        category,

                        condition,

                        minPrice,

                        maxPrice,

                        city,

                        transactionType,

                        language,

                        delivery,

                        listingType,

                        verifiedOnly,

                        sort
                )
        );
    }


    // =========================
    // MY BOOKS
    // =========================

    @GetMapping("/my")
    public ResponseEntity<List<BookResponse>>
            getMyBooks(

                    Authentication authentication) {

        return ResponseEntity.ok(

                bookService.getMyBooks(
                        authentication.getName()
                )
        );
    }


    // =========================
    // GET SINGLE BOOK
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse>
            getBookById(

                    @PathVariable Long id) {

        return ResponseEntity.ok(

                bookService.getBookById(id)
        );
    }


    // =========================
    // UPDATE
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse>
            updateBook(

                    @PathVariable Long id,

                    @RequestBody BookUpdateRequest request,

                    Authentication authentication) {

        return ResponseEntity.ok(

                bookService.updateBook(

                        id,

                        request,

                        authentication.getName()
                )
        );
    }


    // =========================
    // DELETE
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(

            @PathVariable Long id,

            Authentication authentication) {

        bookService.deleteBook(

                id,

                authentication.getName()
        );

        return ResponseEntity.ok(

                "Book deleted successfully"
        );
    }


    // =========================
    // STATUS
    // =========================

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookResponse>
            updateStatus(

                    @PathVariable Long id,

                    @RequestBody BookStatusRequest request,

                    Authentication authentication) {

        return ResponseEntity.ok(

                bookService.updateStatus(

                        id,

                        request,

                        authentication.getName()
                )
        );
    }

    // =========================
    // SELLER STORE & PROFILE
    // =========================

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<BookResponse>> getBooksBySeller(
            @PathVariable Long sellerId) {

        return ResponseEntity.ok(
                bookService.getBooksBySeller(sellerId)
        );
    }

    @GetMapping("/seller/{sellerId}/profile")
    public ResponseEntity<SellerSummaryResponse> getSellerProfile(
            @PathVariable Long sellerId) {

        return ResponseEntity.ok(
                bookService.getSellerProfile(sellerId)
        );
    }
}