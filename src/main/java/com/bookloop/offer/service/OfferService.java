package com.bookloop.offer.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookloop.book.entity.Book;
import com.bookloop.book.repository.BookRepository;
import com.bookloop.offer.dto.OfferCreateRequest;
import com.bookloop.offer.dto.OfferResponse;
import com.bookloop.offer.dto.OfferStatusRequest;
import com.bookloop.offer.entity.Offer;
import com.bookloop.offer.repository.OfferRepository;
import com.bookloop.order.service.OrderService;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;
import com.bookloop.chat.dto.AppNotificationDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    private void sendRealtimeNotification(AppNotificationDto dto) {
        if (messagingTemplate != null) {
            try {
                messagingTemplate.convertAndSend("/topic/notifications", dto);
            } catch (Exception e) {
                // ignore messaging failure
            }
        }
    }

    // =========================================================
    // CREATE OFFER / COUNTER OFFER
    // =========================================================

    @Transactional
    public OfferResponse createOffer(
            OfferCreateRequest request,
            String email) {

        User currentUser = getUser(email);

        // =====================================================
        // COUNTER OFFER
        // =====================================================

        if (request.getParentOfferId() != null) {

            Offer originalOffer =
                    offerRepository
                            .findById(
                                    request.getParentOfferId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Original offer not found"
                                    )
                            );

            // -------------------------------------------------
            // Only original seller can make counter offer
            // -------------------------------------------------

            if (!originalOffer.getSeller()
                    .getId()
                    .equals(currentUser.getId())) {

                throw new RuntimeException(
                        "Only seller can make a counter offer"
                );
            }

            // -------------------------------------------------
            // Original offer must still be pending
            // -------------------------------------------------

            if (!"PENDING".equalsIgnoreCase(
                    originalOffer.getStatus())) {

                throw new RuntimeException(
                        "Only pending offers can be countered"
                );
            }

            // -------------------------------------------------
            // Create COUNTER offer
            // -------------------------------------------------

            Offer counterOffer = new Offer();

            // Same book
            counterOffer.setBook(
                    originalOffer.getBook()
            );

            // IMPORTANT:
            // Counter offer goes back to ORIGINAL BUYER
            counterOffer.setBuyer(
                    originalOffer.getBuyer()
            );

            // Original seller remains seller
            counterOffer.setSeller(
                    originalOffer.getSeller()
            );

            // New counter price
            counterOffer.setAmount(
                    request.getAmount()
            );

            counterOffer.setMessage(
                    request.getMessage()
            );

            // Link counter to original offer
            counterOffer.setParentOfferId(
                    originalOffer.getId()
            );

            // Counter offer waits for buyer's response
            counterOffer.setStatus("COUNTERED");

            LocalDateTime now =
                    LocalDateTime.now();

            counterOffer.setCreatedAt(now);
            counterOffer.setUpdatedAt(now);

            // -------------------------------------------------
            // Mark original offer as COUNTERED
            // -------------------------------------------------

            originalOffer.setStatus("COUNTERED");
            originalOffer.setUpdatedAt(now);

            offerRepository.save(originalOffer);

            // Save counter offer
            Offer savedCounterOffer =
                    offerRepository.save(counterOffer);

            sendRealtimeNotification(AppNotificationDto.builder()
                    .id("notif-" + System.currentTimeMillis())
                    .type("OFFER_COUNTERED")
                    .title("Counter Offer Received")
                    .message(originalOffer.getSeller().getName() + " countered with ₹" + counterOffer.getAmount() + " for \"" + originalOffer.getBook().getTitle() + "\"")
                    .senderId(String.valueOf(originalOffer.getSeller().getId()))
                    .senderName(originalOffer.getSeller().getName())
                    .senderAvatar(originalOffer.getSeller().getAvatar())
                    .targetUserId(String.valueOf(originalOffer.getBuyer().getId()))
                    .targetUserEmail(originalOffer.getBuyer().getEmail())
                    .actionUrl("/dashboard/offers")
                    .time("Just now")
                    .timestamp(System.currentTimeMillis())
                    .build());

            return toResponse(savedCounterOffer);
        }

        // =====================================================
        // NORMAL OFFER
        // =====================================================

        Book book = bookRepository
                .findById(request.getBookId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Book not found"
                        )
                );

        // -----------------------------------------------------
        // Buyer cannot make offer on own book
        // -----------------------------------------------------

        if (book.getSeller()
                .getId()
                .equals(currentUser.getId())) {

            throw new RuntimeException(
                    "You cannot make an offer on your own book"
            );
        }

        // -----------------------------------------------------
        // Book must be active
        // -----------------------------------------------------

        if (!"Active".equalsIgnoreCase(
                book.getStatus())) {

            throw new RuntimeException(
                    "Book is no longer available"
            );
        }

        // -----------------------------------------------------
        // Create normal offer
        // -----------------------------------------------------

        Offer offer = new Offer();

        offer.setBook(book);
        offer.setBuyer(currentUser);
        offer.setSeller(book.getSeller());
        offer.setAmount(request.getAmount());
        offer.setMessage(request.getMessage());

        offer.setParentOfferId(null);

        offer.setStatus("PENDING");

        LocalDateTime now =
                LocalDateTime.now();

        offer.setCreatedAt(now);
        offer.setUpdatedAt(now);

        Offer savedOffer =
                offerRepository.save(offer);

        sendRealtimeNotification(AppNotificationDto.builder()
                .id("notif-" + System.currentTimeMillis())
                .type("OFFER_CREATED")
                .title("New Price Offer")
                .message(currentUser.getName() + " offered ₹" + offer.getAmount() + " for \"" + book.getTitle() + "\"")
                .senderId(String.valueOf(currentUser.getId()))
                .senderName(currentUser.getName())
                .senderAvatar(currentUser.getAvatar())
                .targetUserId(String.valueOf(book.getSeller().getId()))
                .targetUserEmail(book.getSeller().getEmail())
                .actionUrl("/dashboard/offers")
                .time("Just now")
                .timestamp(System.currentTimeMillis())
                .build());

        return toResponse(savedOffer);
    }

    // =========================================================
    // GET SENT OFFERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<OfferResponse> getSentOffers(
            String email) {

        User user = getUser(email);

        return offerRepository
                .findByBuyerOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET RECEIVED OFFERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<OfferResponse> getReceivedOffers(
            String email) {

        User user = getUser(email);

        return offerRepository
                .findBySellerOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // ACCEPT / REJECT / CANCEL OFFER
    // =========================================================

    @Transactional
    public OfferResponse updateStatus(
            Long id,
            OfferStatusRequest request,
            String email) {

        User user = getUser(email);

        Offer offer = offerRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Offer not found"
                        )
                );

        // -----------------------------------------------------
        // Check whether current user is buyer or seller
        // -----------------------------------------------------

        boolean buyer =
                offer.getBuyer()
                        .getId()
                        .equals(user.getId());

        boolean seller =
                offer.getSeller()
                        .getId()
                        .equals(user.getId());

        if (!buyer && !seller) {

            throw new RuntimeException(
                    "You are not part of this offer"
            );
        }

        String status =
                request.getStatus()
                        .toUpperCase();

        // =====================================================
        // SELLER ACTION
        // =====================================================

        if (seller) {

            // Seller can only accept or reject
            // through this endpoint.
            //
            // Counter offer is created through POST /offers
            // with parentOfferId.

            if (!status.equals("ACCEPTED")
                    && !status.equals("REJECTED")) {

                throw new RuntimeException(
                        "Seller can only accept or reject an offer"
                );
            }

            // -------------------------------------------------
            // Only PENDING offer can be accepted/rejected
            // -------------------------------------------------

            if (!"PENDING".equalsIgnoreCase(
                    offer.getStatus())) {

                throw new RuntimeException(
                        "Only pending offers can be accepted or rejected"
                );
            }

            // -------------------------------------------------
            // SELLER ACCEPTS
            // -------------------------------------------------

            if (status.equals("ACCEPTED")) {

                offer.setStatus("ACCEPTED");

                offer.setUpdatedAt(
                        LocalDateTime.now()
                );

                // Save accepted offer first
                offerRepository.save(offer);

                // IMPORTANT:
                // Accepted offer -> Order
                //
                // Order amount will be offer.getAmount()
                // and book will become Sold.
                orderService.createOrderFromOffer(offer);

                sendRealtimeNotification(AppNotificationDto.builder()
                        .id("notif-" + System.currentTimeMillis())
                        .type("OFFER_ACCEPTED")
                        .title("Offer Accepted! 🎉")
                        .message(user.getName() + " accepted your offer of ₹" + offer.getAmount() + " for \"" + offer.getBook().getTitle() + "\"")
                        .senderId(String.valueOf(user.getId()))
                        .senderName(user.getName())
                        .senderAvatar(user.getAvatar())
                        .targetUserId(String.valueOf(offer.getBuyer().getId()))
                        .targetUserEmail(offer.getBuyer().getEmail())
                        .actionUrl("/dashboard/orders")
                        .time("Just now")
                        .timestamp(System.currentTimeMillis())
                        .build());

                return toResponse(offer);
            }

            // -------------------------------------------------
            // SELLER REJECTS
            // -------------------------------------------------

            offer.setStatus("REJECTED");

            offer.setUpdatedAt(
                    LocalDateTime.now()
            );

            Offer savedRejected = offerRepository.save(offer);

            sendRealtimeNotification(AppNotificationDto.builder()
                    .id("notif-" + System.currentTimeMillis())
                    .type("OFFER_REJECTED")
                    .title("Offer Declined")
                    .message(user.getName() + " declined your offer for \"" + offer.getBook().getTitle() + "\"")
                    .senderId(String.valueOf(user.getId()))
                    .senderName(user.getName())
                    .senderAvatar(user.getAvatar())
                    .targetUserId(String.valueOf(offer.getBuyer().getId()))
                    .targetUserEmail(offer.getBuyer().getEmail())
                    .actionUrl("/dashboard/offers")
                    .time("Just now")
                    .timestamp(System.currentTimeMillis())
                    .build());

            return toResponse(savedRejected);
        }

        // =====================================================
        // BUYER ACTION
        // =====================================================

        if (buyer) {

            // -------------------------------------------------
            // Buyer cancels normal pending offer
            // -------------------------------------------------

            if (status.equals("CANCELLED")) {

                if (!"PENDING".equalsIgnoreCase(
                        offer.getStatus())) {

                    throw new RuntimeException(
                            "Only pending offers can be cancelled"
                    );
                }

                offer.setStatus("CANCELLED");

                offer.setUpdatedAt(
                        LocalDateTime.now()
                );

                return toResponse(
                        offerRepository.save(offer)
                );
            }

            // -------------------------------------------------
            // Buyer responds to COUNTER OFFER
            // -------------------------------------------------

            if (status.equals("ACCEPTED")
                    || status.equals("REJECTED")) {

                // This must be a counter offer
                if (offer.getParentOfferId() == null
                        || !"COUNTERED".equalsIgnoreCase(
                                offer.getStatus())) {

                    throw new RuntimeException(
                            "Buyer can only accept or reject a counter offer"
                    );
                }

                // -------------------------------------------------
                // BUYER ACCEPTS COUNTER
                // -------------------------------------------------

                if (status.equals("ACCEPTED")) {

                    offer.setStatus("ACCEPTED");

                    offer.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    // Save accepted counter
                    offerRepository.save(offer);

                    // IMPORTANT:
                    // Accepted counter -> Order
                    //
                    // Order amount = counter offer amount.
                    orderService.createOrderFromOffer(offer);

                    sendRealtimeNotification(AppNotificationDto.builder()
                            .id("notif-" + System.currentTimeMillis())
                            .type("OFFER_ACCEPTED")
                            .title("Counter Offer Accepted! 🎉")
                            .message(user.getName() + " accepted your counter offer for \"" + offer.getBook().getTitle() + "\"")
                            .senderId(String.valueOf(user.getId()))
                            .senderName(user.getName())
                            .senderAvatar(user.getAvatar())
                            .targetUserId(String.valueOf(offer.getSeller().getId()))
                            .targetUserEmail(offer.getSeller().getEmail())
                            .actionUrl("/dashboard/orders")
                            .time("Just now")
                            .timestamp(System.currentTimeMillis())
                            .build());

                    return toResponse(offer);
                }

                // -------------------------------------------------
                // BUYER REJECTS COUNTER
                // -------------------------------------------------

                offer.setStatus("REJECTED");

                offer.setUpdatedAt(
                        LocalDateTime.now()
                );

                Offer savedRejectedCounter = offerRepository.save(offer);

                sendRealtimeNotification(AppNotificationDto.builder()
                        .id("notif-" + System.currentTimeMillis())
                        .type("OFFER_REJECTED")
                        .title("Counter Offer Declined")
                        .message(user.getName() + " declined your counter offer for \"" + offer.getBook().getTitle() + "\"")
                        .senderId(String.valueOf(user.getId()))
                        .senderName(user.getName())
                        .senderAvatar(user.getAvatar())
                        .targetUserId(String.valueOf(offer.getSeller().getId()))
                        .targetUserEmail(offer.getSeller().getEmail())
                        .actionUrl("/dashboard/offers")
                        .time("Just now")
                        .timestamp(System.currentTimeMillis())
                        .build());

                return toResponse(savedRejectedCounter);
            }

            throw new RuntimeException(
                    "Invalid buyer action"
            );
        }

        throw new RuntimeException(
                "Invalid offer action"
        );
    }

    // =========================================================
    // GET USER
    // =========================================================

    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }

    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private OfferResponse toResponse(
            Offer offer) {

        OfferResponse response =
                new OfferResponse();

        response.setId(
                offer.getId()
        );

        // =====================================================
        // BUYER
        // =====================================================

        OfferResponse.BuyerInfo buyerInfo =
                new OfferResponse.BuyerInfo();

        buyerInfo.setId(
                offer.getBuyer().getId()
        );

        buyerInfo.setName(
                offer.getBuyer().getName()
        );

        buyerInfo.setAvatar(
                offer.getBuyer().getAvatar()
        );

        buyerInfo.setLocation(
                offer.getBuyer().getCity()
        );

        response.setBuyer(buyerInfo);

        // =====================================================
        // BOOK
        // =====================================================

        OfferResponse.BookInfo bookInfo =
                new OfferResponse.BookInfo();

        bookInfo.setId(
                offer.getBook().getId()
        );

        bookInfo.setTitle(
                offer.getBook().getTitle()
        );

        bookInfo.setOriginalPrice(
                offer.getBook().getPrice()
        );

        // Frontend already has fallback image
        bookInfo.setImage(null);

        response.setBook(bookInfo);

        // =====================================================
        // OFFER
        // =====================================================

        response.setOfferPrice(
                offer.getAmount()
        );

        response.setMessage(
                offer.getMessage()
        );

        response.setStatus(
                formatStatus(
                        offer.getStatus()
                )
        );

        response.setParentOfferId(
                offer.getParentOfferId()
        );

        response.setCreatedAt(
                offer.getCreatedAt()
        );

        response.setUpdatedAt(
                offer.getUpdatedAt()
        );

        response.setDate(
                formatDate(
                        offer.getCreatedAt()
                )
        );

        return response;
    }

    // =========================================================
    // FORMAT STATUS FOR FRONTEND
    // =========================================================

    private String formatStatus(String status) {

        if (status == null) {
            return null;
        }

        String normalized =
                status.toUpperCase();

        return switch (normalized) {

            case "PENDING" ->
                    "Pending";

            case "ACCEPTED" ->
                    "Accepted";

            case "REJECTED" ->
                    "Rejected";

            case "COUNTERED" ->
                    "Countered";

            case "CANCELLED" ->
                    "Cancelled";

            default ->
                    status;
        };
    }

    // =========================================================
    // FORMAT DATE
    // =========================================================

    private String formatDate(
            LocalDateTime dateTime) {

        if (dateTime == null) {
            return null;
        }

        return dateTime
                .toLocalDate()
                .toString();
    }
}