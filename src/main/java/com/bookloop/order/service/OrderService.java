package com.bookloop.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookloop.book.entity.Book;
import com.bookloop.book.repository.BookRepository;
import com.bookloop.offer.entity.Offer;
import com.bookloop.order.dto.OrderCreateRequest;
import com.bookloop.order.dto.OrderResponse;
import com.bookloop.order.dto.OrderStatusRequest;
import com.bookloop.order.entity.Order;
import com.bookloop.order.repository.OrderRepository;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final BookRepository bookRepository;

    private final UserRepository userRepository;


    // =========================================================
    // CREATE ORDER FROM ACCEPTED OFFER
    // =========================================================

    @Transactional
    public OrderResponse createOrderFromOffer(
            Offer offer) {

        if (offer == null) {
            throw new RuntimeException(
                    "Offer is required"
            );
        }


        Book book =
                offer.getBook();

        User buyer =
                offer.getBuyer();

        User seller =
                offer.getSeller();


        // -----------------------------------------------------
        // Safety checks
        // -----------------------------------------------------

        if (book == null) {
            throw new RuntimeException(
                    "Offer book not found"
            );
        }

        if (buyer == null) {
            throw new RuntimeException(
                    "Offer buyer not found"
            );
        }

        if (seller == null) {
            throw new RuntimeException(
                    "Offer seller not found"
            );
        }


        // -----------------------------------------------------
        // Buyer and seller cannot be same
        // -----------------------------------------------------

        if (buyer.getId()
                .equals(seller.getId())) {

            throw new RuntimeException(
                    "Buyer and seller cannot be the same user"
            );
        }


        // -----------------------------------------------------
        // Book must still be active
        // -----------------------------------------------------

        if (!"Active".equalsIgnoreCase(
                book.getStatus())) {

            throw new RuntimeException(
                    "Book is no longer available"
            );
        }


        // -----------------------------------------------------
        // Offer amount must exist
        // -----------------------------------------------------

        if (offer.getAmount() == null
                || offer.getAmount().signum() <= 0) {

            throw new RuntimeException(
                    "Invalid offer amount"
            );
        }


        // -----------------------------------------------------
        // Create Order
        // -----------------------------------------------------

        Order order =
                new Order();


        order.setBook(
                book
        );

        order.setBuyer(
                buyer
        );

        order.setSeller(
                seller
        );


        // IMPORTANT:
        //
        // Accepted offer amount becomes
        // the actual order amount.
        //
        // Example:
        //
        // Book price  = 1000
        // Offer       = 650
        // Order       = 650

        order.setAmount(
                offer.getAmount()
        );


        // Offer accept ke time frontend
        // delivery address nahi bhej raha.

        order.setDeliveryAddress(
                null
        );


        order.setStatus(
                "PENDING"
        );


        order.setOrderNumber(
                generateOrderNumber()
        );


        LocalDateTime now =
                LocalDateTime.now();


        order.setCreatedAt(
                now
        );

        order.setUpdatedAt(
                now
        );


        // -----------------------------------------------------
        // Book becomes SOLD
        // -----------------------------------------------------

        book.setStatus(
                "Sold"
        );

        book.setUpdatedAt(
                now
        );


        bookRepository.save(
                book
        );


        // -----------------------------------------------------
        // Save Order
        // -----------------------------------------------------

        Order savedOrder =
                orderRepository.save(
                        order
                );


        return toResponse(
                savedOrder
        );
    }


    // =========================================================
    // DIRECT BUY / NORMAL ORDER
    // =========================================================

    @Transactional
    public OrderResponse createOrder(
            OrderCreateRequest request,
            String email) {

        if (request == null
                || request.getBookId() == null) {

            throw new RuntimeException(
                    "Book ID is required"
            );
        }


        User buyer =
                getUser(email);


        Book book =
                bookRepository
                        .findById(
                                request.getBookId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Book not found"
                                )
                        );


        // -----------------------------------------------------
        // Cannot buy own book
        // -----------------------------------------------------

        if (book.getSeller()
                .getId()
                .equals(buyer.getId())) {

            throw new RuntimeException(
                    "You cannot buy your own book"
            );
        }


        // -----------------------------------------------------
        // Book must be active
        // -----------------------------------------------------

        if (!"Active".equalsIgnoreCase(
                book.getStatus())) {

            throw new RuntimeException(
                    "Book is not available"
            );
        }


        // -----------------------------------------------------
        // Create order
        // -----------------------------------------------------

        Order order =
                new Order();


        order.setBook(
                book
        );

        order.setBuyer(
                buyer
        );

        order.setSeller(
                book.getSeller()
        );


        // Direct purchase uses
        // current book price.

        order.setAmount(
                book.getPrice()
        );


        order.setDeliveryAddress(
                request.getDeliveryAddress()
        );


        order.setStatus(
                "PENDING"
        );


        order.setOrderNumber(
                generateOrderNumber()
        );


        LocalDateTime now =
                LocalDateTime.now();


        order.setCreatedAt(
                now
        );

        order.setUpdatedAt(
                now
        );


        // -----------------------------------------------------
        // Book becomes SOLD
        // -----------------------------------------------------

        book.setStatus(
                "Sold"
        );

        book.setUpdatedAt(
                now
        );


        bookRepository.save(
                book
        );


        // -----------------------------------------------------
        // Save order
        // -----------------------------------------------------

        Order savedOrder =
                orderRepository.save(
                        order
                );


        return toResponse(
                savedOrder
        );
    }


    // =========================================================
    // BUYER ORDERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(
            String email) {

        User buyer =
                getUser(email);


        return orderRepository
                .findByBuyerOrderByCreatedAtDesc(
                        buyer
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // SELLER ORDERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<OrderResponse> getSellerOrders(
            String email) {

        User seller =
                getUser(email);


        return orderRepository
                .findBySellerOrderByCreatedAtDesc(
                        seller
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET SINGLE ORDER
    // =========================================================

    @Transactional(readOnly = true)
    public OrderResponse getOrder(
            Long id,
            String email) {

        User user =
                getUser(email);


        Order order =
                orderRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                )
                        );


        boolean buyer =
                order.getBuyer()
                        .getId()
                        .equals(user.getId());


        boolean seller =
                order.getSeller()
                        .getId()
                        .equals(user.getId());


        if (!buyer && !seller) {

            throw new RuntimeException(
                    "You are not allowed to view this order"
            );
        }


        return toResponse(
                order
        );
    }


    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    @Transactional
    public OrderResponse updateStatus(
            Long id,
            OrderStatusRequest request,
            String email) {

        if (request == null
                || request.getStatus() == null
                || request.getStatus().isBlank()) {

            throw new RuntimeException(
                    "Order status is required"
            );
        }


        User user =
                getUser(email);


        Order order =
                orderRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                )
                        );


        // -----------------------------------------------------
        // Only seller can update order status
        // -----------------------------------------------------

        if (!order.getSeller()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "Only seller can update order status"
            );
        }


        String status =
                request.getStatus()
                        .trim()
                        .toUpperCase();


        // -----------------------------------------------------
        // Validate status
        // -----------------------------------------------------

        if (!isValidOrderStatus(status)) {

            throw new RuntimeException(
                    "Invalid order status: "
                            + status
            );
        }


        order.setStatus(
                status
        );

        order.setUpdatedAt(
                LocalDateTime.now()
        );


        return toResponse(
                orderRepository.save(
                        order
                )
        );
    }


    // =========================================================
    // CANCEL ORDER
    // =========================================================

    @Transactional
    public OrderResponse cancelOrder(
            Long id,
            String email) {

        User user =
                getUser(email);


        Order order =
                orderRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                )
                        );


        // -----------------------------------------------------
        // Only buyer can cancel
        // -----------------------------------------------------

        if (!order.getBuyer()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "Only buyer can cancel the order"
            );
        }


        // -----------------------------------------------------
        // Completed / delivered cannot be cancelled
        // -----------------------------------------------------

        if ("COMPLETED".equalsIgnoreCase(
                order.getStatus())
                ||
                "DELIVERED".equalsIgnoreCase(
                        order.getStatus())) {

            throw new RuntimeException(
                    "Completed order cannot be cancelled"
            );
        }


        order.setStatus(
                "CANCELLED"
        );

        order.setUpdatedAt(
                LocalDateTime.now()
        );


        return toResponse(
                orderRepository.save(
                        order
                )
        );
    }


    // =========================================================
    // GET USER
    // =========================================================

    private User getUser(
            String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }


    // =========================================================
    // VALIDATE ORDER STATUS
    // =========================================================

    private boolean isValidOrderStatus(
            String status) {

        return status.equals("PENDING")
                || status.equals("CONFIRMED")
                || status.equals("SHIPPED")
                || status.equals("OUT_FOR_DELIVERY")
                || status.equals("COMPLETED")
                || status.equals("DELIVERED")
                || status.equals("CANCELLED");
    }


    // =========================================================
    // GENERATE ORDER NUMBER
    // =========================================================

    private String generateOrderNumber() {

        return "BL-"
                + UUID.randomUUID()
                        .toString()
                        .substring(
                                0,
                                8
                        )
                        .toUpperCase();
    }


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private OrderResponse toResponse(
            Order order) {

        OrderResponse response =
                new OrderResponse();


        // -----------------------------------------------------
        // ORDER
        // -----------------------------------------------------

        response.setId(
                order.getId()
        );

        response.setOrderNumber(
                order.getOrderNumber()
        );


        // -----------------------------------------------------
        // BOOK
        // -----------------------------------------------------

        Book book =
                order.getBook();


        response.setBookId(
                book.getId()
        );

        response.setBookTitle(
                book.getTitle()
        );

        response.setBookAuthor(
                book.getAuthor()
        );


        // -----------------------------------------------------
        // BOOK IMAGE
        // -----------------------------------------------------

        if (book.getImages() != null
                && !book.getImages().isEmpty()) {

            response.setBookImage(
                    book.getImages().get(0)
            );
        } else {

            response.setBookImage(
                    null
            );
        }


        // -----------------------------------------------------
        // ORIGINAL PRICE
        // -----------------------------------------------------

        response.setOriginalPrice(
                book.getOriginalPrice()
        );


        // -----------------------------------------------------
        // CONDITION
        // -----------------------------------------------------

        response.setCondition(
                book.getCondition()
        );


        // -----------------------------------------------------
        // CATEGORY
        // -----------------------------------------------------

        response.setCategory(
                book.getCategory()
        );


        // -----------------------------------------------------
        // BUYER
        // -----------------------------------------------------

        response.setBuyerId(
                order.getBuyer().getId()
        );

        response.setBuyerName(
                order.getBuyer().getName()
        );

        response.setBuyerAvatar(
                order.getBuyer().getAvatar()
        );


        // -----------------------------------------------------
        // SELLER
        // -----------------------------------------------------

        response.setSellerId(
                order.getSeller().getId()
        );

        response.setSellerName(
                order.getSeller().getName()
        );

        response.setSellerAvatar(
                order.getSeller().getAvatar()
        );


        // -----------------------------------------------------
        // ORDER AMOUNT
        // -----------------------------------------------------

        response.setAmount(
                order.getAmount()
        );


        // -----------------------------------------------------
        // DELIVERY ADDRESS
        // -----------------------------------------------------

        response.setDeliveryAddress(
                order.getDeliveryAddress()
        );


        // -----------------------------------------------------
        // TRACKING NUMBER
        // -----------------------------------------------------

        // orderNumber already starts with BL-
        // so we use it directly.

        response.setTrackingNumber(
                order.getOrderNumber()
        );


        // -----------------------------------------------------
        // DELIVERY METHOD
        // -----------------------------------------------------

        response.setDeliveryMethod(
                "BookLoop Marketplace"
        );


        // -----------------------------------------------------
        // ESTIMATED DELIVERY
        // -----------------------------------------------------

        response.setEstimatedDelivery(
                getEstimatedDelivery(
                        order.getStatus()
                )
        );


        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        response.setStatus(
                order.getStatus()
        );


        // -----------------------------------------------------
        // DATES
        // -----------------------------------------------------

        response.setCreatedAt(
                order.getCreatedAt()
        );

        response.setUpdatedAt(
                order.getUpdatedAt()
        );


        return response;
    }


    // =========================================================
    // ESTIMATED DELIVERY
    // =========================================================

    private String getEstimatedDelivery(
            String status) {

        if (status == null) {
            return "Awaiting seller confirmation";
        }


        return switch (
                status.toUpperCase()
        ) {

            case "PENDING" ->
                    "Awaiting seller confirmation";

            case "CONFIRMED" ->
                    "Seller preparing the book";

            case "SHIPPED" ->
                    "Book is on the way";

            case "OUT_FOR_DELIVERY" ->
                    "Arriving today";

            case "COMPLETED",
                 "DELIVERED" ->
                    "Delivered successfully";

            case "CANCELLED" ->
                    "Order cancelled";

            default ->
                    "In Progress";
        };
    }
}