package com.bookloop.book.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookloop.book.dto.BookCreateRequest;
import com.bookloop.book.dto.BookResponse;
import com.bookloop.book.dto.BookStatusRequest;
import com.bookloop.book.dto.BookUpdateRequest;
import com.bookloop.book.dto.SellerSummaryResponse;
import com.bookloop.book.entity.Book;
import com.bookloop.book.repository.BookRepository;
import com.bookloop.favorite.repository.FavoriteRepository;
import com.bookloop.user.entity.Role;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;
import com.bookloop.review.repository.ReviewRepository;

import jakarta.persistence.criteria.Join;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    private final UserRepository userRepository;

    private final FavoriteRepository favoriteRepository;
    
    private final ReviewRepository reviewRepository;


    // =========================
    // CREATE BOOK
    // =========================

    @Transactional
    public BookResponse createBook(
            BookCreateRequest request,
            String email) {

        User seller = getUser(email);

        Book book = new Book();

        copyCreateData(request, book);

        book.setSeller(seller);

        book.setStatus("Active");

        book.setViews(0L);

        LocalDateTime now = LocalDateTime.now();

        book.setCreatedAt(now);

        book.setUpdatedAt(now);

        if (request.getImages() != null) {

            book.setImages(
                    new ArrayList<>(request.getImages())
            );

        } else {

            book.setImages(new ArrayList<>());
        }

        Book savedBook = bookRepository.save(book);

        return convertToResponse(savedBook);
    }


    // =========================
    // GET ALL MARKETPLACE BOOKS
    // =========================

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {

        List<Book> books =
                bookRepository.findByStatus("Active");

        return books.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================
    // POPULAR BOOKS
    // =========================

    @Transactional(readOnly = true)
    public List<BookResponse> getPopularBooks() {

        List<Book> books =
                bookRepository
                        .findTop10ByStatusOrderByViewsDescCreatedAtDesc(
                                "Active"
                        );

        return books.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================
    // LATEST BOOKS
    // =========================

    @Transactional(readOnly = true)
    public List<BookResponse> getLatestBooks() {

        List<Book> books =
                bookRepository
                        .findTop10ByStatusOrderByCreatedAtDesc(
                                "Active"
                        );

        return books.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================
    // BOOKS NEAR YOU
    // =========================

    @Transactional(readOnly = true)
    public List<BookResponse> getBooksNearYou(
            String city) {

        if (city == null || city.isBlank()) {

            return getLatestBooks();
        }

        List<Book> books =
                bookRepository
                        .findByStatusAndCityIgnoreCaseOrderByCreatedAtDesc(
                                "Active",
                                city.trim()
                        );

        return books.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================
    // GET BOOK BY ID
    // =========================

    @Transactional
    public BookResponse getBookById(Long id) {

        Book book = bookRepository.findById(id)

                .orElseThrow(() ->
                        new RuntimeException(
                                "Book not found with id: " + id
                        )
                );

        // Increase views
        book.setViews(
                book.getViews() == null
                        ? 1L
                        : book.getViews() + 1
        );

        bookRepository.save(book);

        return convertToResponse(book);
    }


    // =========================
    // MY LISTINGS
    // =========================

    @Transactional(readOnly = true)
    public List<BookResponse> getMyBooks(
            String email) {

        User seller = getUser(email);

        List<Book> books =
                bookRepository.findBySeller(seller);

        return books.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================
    // UPDATE BOOK
    // =========================

    @Transactional
    public BookResponse updateBook(
            Long id,
            BookUpdateRequest request,
            String email) {

        User seller = getUser(email);

        Book book = bookRepository.findById(id)

                .orElseThrow(() ->
                        new RuntimeException(
                                "Book not found with id: " + id
                        )
                );

        checkOwnership(book, seller);

        copyUpdateData(request, book);

        book.setUpdatedAt(
                LocalDateTime.now()
        );

        if (request.getImages() != null) {

            book.setImages(
                    new ArrayList<>(request.getImages())
            );
        }

        Book updatedBook =
                bookRepository.save(book);

        return convertToResponse(updatedBook);
    }


    // =========================
    // DELETE BOOK
    // =========================

    @Transactional
    public void deleteBook(
            Long id,
            String email) {

        User seller = getUser(email);

        Book book = bookRepository.findById(id)

                .orElseThrow(() ->
                        new RuntimeException(
                                "Book not found with id: " + id
                        )
                );

        checkOwnership(book, seller);

        bookRepository.delete(book);
    }


    // =========================
    // UPDATE STATUS
    // =========================

    @Transactional
    public BookResponse updateStatus(
            Long id,
            BookStatusRequest request,
            String email) {

        User seller = getUser(email);

        Book book = bookRepository.findById(id)

                .orElseThrow(() ->
                        new RuntimeException(
                                "Book not found with id: " + id
                        )
                );

        checkOwnership(book, seller);

        book.setStatus(request.getStatus());

        book.setUpdatedAt(
                LocalDateTime.now()
        );

        return convertToResponse(
                bookRepository.save(book)
        );
    }


    // =========================
    // SEARCH + FILTER
    // =========================

    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(

            String keyword,

            String category,

            String condition,

            BigDecimal minPrice,

            BigDecimal maxPrice,

            String city,

            String transactionType,

            String language,

            String delivery,

            String listingType,

            Boolean verifiedOnly,

            String sort) {

        Specification<Book> specification =
                Specification.where(
                        hasStatus("Active")
                );


        // =========================
        // KEYWORD
        // =========================

        if (keyword != null &&
                !keyword.isBlank()) {

            String value =
                    "%" + keyword.toLowerCase() + "%";

            specification = specification.and(

                    (root, query, cb) ->

                            cb.or(

                                    cb.like(
                                            cb.lower(
                                                    root.get("title")
                                            ),
                                            value
                                    ),

                                    cb.like(
                                            cb.lower(
                                                    root.get("author")
                                            ),
                                            value
                                    ),

                                    cb.like(
                                            cb.lower(
                                                    root.get("description")
                                            ),
                                            value
                                    )
                            )
            );
        }


        // =========================
        // CATEGORY
        // =========================

        if (category != null &&
                !category.isBlank()) {

            specification = specification.and(

                    (root, query, cb) ->

                            cb.equal(
                                    cb.lower(
                                            root.get("category")
                                    ),
                                    category.toLowerCase()
                            )
            );
        }


        // =========================
        // CONDITION
        // =========================

        if (condition != null &&
                !condition.isBlank()) {

            specification = specification.and(

                    (root, query, cb) ->

                            cb.equal(
                                    cb.lower(
                                            root.get("condition")
                                    ),
                                    condition.toLowerCase()
                            )
            );
        }


        // =========================
        // MIN PRICE
        // =========================

        if (minPrice != null) {

            specification = specification.and(

                    (root, query, cb) ->

                            cb.greaterThanOrEqualTo(
                                    root.get("price"),
                                    minPrice
                            )
            );
        }


        // =========================
        // MAX PRICE
        // =========================

        if (maxPrice != null) {

            specification = specification.and(

                    (root, query, cb) ->

                            cb.lessThanOrEqualTo(
                                    root.get("price"),
                                    maxPrice
                            )
            );
        }


     // =========================
     // CITY
     // =========================

     if (city != null && !city.isBlank()) {

         String cityName = city
                 .split(",")[0]
                 .trim()
                 .toLowerCase();

         specification = specification.and(

                 (root, query, cb) ->
                         cb.like(
                                 cb.lower(root.get("city")),
                                 cityName + "%"
                         )
         );
     }


        // =========================
        // TRANSACTION TYPE
        // =========================

        if (transactionType != null &&
                !transactionType.isBlank()) {

            specification = specification.and(

                    (root, query, cb) ->

                            cb.equal(
                                    cb.lower(
                                            root.get("transactionType")
                                    ),
                                    transactionType.toLowerCase()
                            )
            );
        }


        // =========================
        // LANGUAGE
        // =========================

        if (language != null &&
                !language.isBlank()) {

            specification = specification.and(

                    (root, query, cb) ->

                            cb.equal(
                                    cb.lower(
                                            root.get("language")
                                    ),
                                    language.toLowerCase()
                            )
            );
        }


        // =========================
        // DELIVERY
        // =========================

        if (delivery != null &&
                !delivery.isBlank()) {

            specification = specification.and(

                    (root, query, cb) ->

                            cb.equal(
                                    cb.lower(
                                            root.get("delivery")
                                    ),
                                    delivery.toLowerCase()
                            )
            );
        }


        // =========================
        // LISTING TYPE - NEW
        // =========================

        if (listingType != null &&
                !listingType.isBlank()) {

            specification = specification.and(

                    (root, query, cb) ->

                            cb.equal(
                                    cb.lower(
                                            root.get("listingType")
                                    ),
                                    listingType.toLowerCase()
                            )
            );
        }


        // =========================
        // VERIFIED SELLER - NEW
        // =========================

        if (Boolean.TRUE.equals(verifiedOnly)) {

            specification = specification.and(

                    (root, query, cb) -> {

                        Join<Book, User> sellerJoin =
                                root.join("seller");

                        return cb.isTrue(
                                sellerJoin.get("verified")
                        );
                    }
            );
        }


        // =========================
        // SORTING
        // =========================

        Sort sorting =
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                );

        if ("price_asc".equalsIgnoreCase(sort)) {

            sorting =
                    Sort.by(
                            Sort.Direction.ASC,
                            "price"
                    );

        } else if ("price_desc".equalsIgnoreCase(sort)) {

            sorting =
                    Sort.by(
                            Sort.Direction.DESC,
                            "price"
                    );

        } else if ("oldest".equalsIgnoreCase(sort)) {

            sorting =
                    Sort.by(
                            Sort.Direction.ASC,
                            "createdAt"
                    );
        }


        List<Book> books =
                bookRepository.findAll(
                        specification,
                        sorting
                );

        return books.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================
    // HELPERS
    // =========================

    private User getUser(String email) {

        return userRepository.findByEmail(email)

                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }


    private void checkOwnership(
            Book book,
            User user) {

        if (!book.getSeller()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to modify this book"
            );
        }
    }


    private void copyCreateData(
            BookCreateRequest request,
            Book book) {

        book.setTitle(request.getTitle());

        book.setAuthor(request.getAuthor());

        book.setPrice(request.getPrice());

        book.setOriginalPrice(
                request.getOriginalPrice()
        );

        book.setCondition(
                request.getCondition()
        );

        book.setCategory(
                request.getCategory()
        );

        book.setListingType(
                request.getListingType()
        );

        book.setTransactionType(
                request.getTransactionType()
        );

        book.setExchangePreferences(
                request.getExchangePreferences()
        );

        book.setNegotiable(
                request.isNegotiable()
        );

        book.setLocation(
                request.getLocation()
        );

        String city = request.getCity();

        if (city != null && !city.isBlank()) {

            city = city
                    .split(",")[0]
                    .trim();
        }

        book.setCity(city);

        book.setLanguage(
                request.getLanguage()
        );

        book.setDelivery(
                request.getDelivery()
        );

        book.setIsbn(
                request.getIsbn()
        );

        book.setEdition(
                request.getEdition()
        );

        book.setPublisher(
                request.getPublisher()
        );

        book.setPages(
                request.getPages()
        );

        book.setPublicationYear(
                request.getPublicationYear()
        );

        book.setDescription(
                request.getDescription()
        );
    }


    private void copyUpdateData(
            BookUpdateRequest request,
            Book book) {

        book.setTitle(
                request.getTitle()
        );

        book.setAuthor(
                request.getAuthor()
        );

        book.setPrice(
                request.getPrice()
        );

        book.setOriginalPrice(
                request.getOriginalPrice()
        );

        book.setCondition(
                request.getCondition()
        );

        book.setCategory(
                request.getCategory()
        );

        book.setListingType(
                request.getListingType()
        );

        book.setTransactionType(
                request.getTransactionType()
        );

        book.setExchangePreferences(
                request.getExchangePreferences()
        );

        book.setNegotiable(
                request.isNegotiable()
        );

        book.setLocation(
                request.getLocation()
        );

        book.setCity(
                request.getCity()
        );

        book.setLanguage(
                request.getLanguage()
        );

        book.setDelivery(
                request.getDelivery()
        );

        book.setIsbn(
                request.getIsbn()
        );

        book.setEdition(
                request.getEdition()
        );

        book.setPublisher(
                request.getPublisher()
        );

        book.setPages(
                request.getPages()
        );

        book.setPublicationYear(
                request.getPublicationYear()
        );

        book.setDescription(
                request.getDescription()
        );
    }


    private Specification<Book> hasStatus(
            String status) {

        return (root, query, cb) ->

                cb.equal(
                        root.get("status"),
                        status
                );
    }


    // =========================
    // CONVERT ENTITY -> RESPONSE
    // =========================

    public BookResponse convertToResponse(
            Book book) {

        BookResponse response =
                new BookResponse();

        response.setId(
                book.getId()
        );

        response.setTitle(
                book.getTitle()
        );

        response.setAuthor(
                book.getAuthor()
        );

        response.setPrice(
                book.getPrice()
        );

        response.setOriginalPrice(
                book.getOriginalPrice()
        );

        response.setCondition(
                book.getCondition()
        );

        response.setCategory(
                book.getCategory()
        );

        response.setListingType(
                book.getListingType()
        );

        response.setTransactionType(
                book.getTransactionType()
        );

        response.setExchangePreferences(
                book.getExchangePreferences()
        );

        response.setNegotiable(
                book.isNegotiable()
        );

        response.setLocation(
                book.getLocation()
        );

        response.setCity(
                book.getCity()
        );

        response.setLanguage(
                book.getLanguage()
        );

        response.setDelivery(
                book.getDelivery()
        );

        response.setIsbn(
                book.getIsbn()
        );

        response.setEdition(
                book.getEdition()
        );

        response.setPublisher(
                book.getPublisher()
        );

        response.setPages(
                book.getPages()
        );

        response.setPublicationYear(
                book.getPublicationYear()
        );

        response.setDescription(
                book.getDescription()
        );

        response.setImages(
                book.getImages()
        );

        response.setViews(
                book.getViews()
        );

        response.setStatus(
                book.getStatus()
        );


        // =========================
        // FAVORITES COUNT
        // =========================

        response.setFavoritesCount(
                favoriteRepository.countByBook(book)
        );


        response.setCreatedAt(
                book.getCreatedAt()
        );

        response.setUpdatedAt(
                book.getUpdatedAt()
        );


        // =========================
        // SELLER SUMMARY
        // =========================

        if (book.getSeller() != null) {

            SellerSummaryResponse seller =
                    new SellerSummaryResponse();

            seller.setId(
                    book.getSeller().getId()
            );

            seller.setName(
                    book.getSeller().getName()
            );

            seller.setEmail(
                    book.getSeller().getEmail()
            );

            seller.setAvatar(
                    book.getSeller().getAvatar()
            );

            if (book.getSeller().getRole() != null) {

                seller.setRole(
                        book.getSeller()
                                .getRole()
                                .name()
                );

                seller.setBusiness(
                        book.getSeller()
                                .getRole() == Role.SELLER
                );
            }

            seller.setVerified(
                    book.getSeller().isVerified()
            );
            
            
            // =========================
            // RATING + REVIEWS
            // =========================

            Double rating =
                    reviewRepository.getAverageRating(
                            book.getSeller()
                    );

            Long reviewsCount =
                    reviewRepository.countBySeller(
                            book.getSeller()
                    );

            seller.setRating(
                    rating == null ? 0.0 : rating
            );

            seller.setReviewsCount(
                    reviewsCount
            );

            response.setSeller(seller);
        }

        return response;
    }

    // =========================
    // SELLER STORE & PROFILE
    // =========================

    public List<BookResponse> getBooksBySeller(Long sellerId) {
        return bookRepository.findBySellerIdOrderByCreatedAtDesc(sellerId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    public SellerSummaryResponse getSellerProfile(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found with id: " + sellerId));

        SellerSummaryResponse res = new SellerSummaryResponse();
        res.setId(seller.getId());
        res.setName(seller.getName());
        res.setEmail(seller.getEmail());
        res.setAvatar(seller.getAvatar());
        if (seller.getRole() != null) {
            res.setRole(seller.getRole().name());
            res.setBusiness(seller.getRole() == Role.SELLER);
        }
        res.setVerified(seller.isVerified());
        res.setLocation(seller.getLocation() != null && !seller.getLocation().isBlank()
                ? seller.getLocation()
                : (seller.getCity() != null ? seller.getCity() : "Noida, UP"));

        Double rating = reviewRepository.getAverageRating(seller);
        res.setRating(rating != null ? rating : 4.8);
        res.setReviewsCount(reviewRepository.countBySeller(seller));

        long totalListings = bookRepository.countBySeller(seller);
        long soldCount = bookRepository.countBySellerAndStatus(seller, "Sold");
        res.setTotalListings(totalListings);
        res.setSoldCount(soldCount);
        res.setResponseRate("< 15 mins");

        if (seller.getCreatedAt() != null) {
            res.setMemberSince(seller.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")));
        } else {
            res.setMemberSince("March 2024");
        }

        return res;
    }
}