package com.bookloop.exchange.service;
 
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookloop.book.entity.Book;
import com.bookloop.book.repository.BookRepository;
import com.bookloop.exchange.dto.ExchangeCreateRequest;
import com.bookloop.exchange.dto.ExchangeResponse;
import com.bookloop.exchange.dto.ExchangeStatusRequest;
import com.bookloop.exchange.entity.Exchange;
import com.bookloop.exchange.repository.ExchangeRepository;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;
import com.bookloop.chat.dto.AppNotificationDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

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


    @Transactional
    public ExchangeResponse createExchange(
            ExchangeCreateRequest request,
            String email) {

        User requester = getUser(email);

        Book offeredBook =
                getBook(request.getOfferedBookId());

        Book requestedBook =
                getBook(request.getRequestedBookId());

        if (!offeredBook.getSeller()
                .getId()
                .equals(requester.getId())) {

            throw new RuntimeException(
                "You can only offer your own book"
            );
        }

        if (requestedBook.getSeller()
                .getId()
                .equals(requester.getId())) {

            throw new RuntimeException(
                "You cannot exchange with your own book"
            );
        }

        Exchange exchange =
                new Exchange();

        exchange.setOfferedBook(offeredBook);
        exchange.setRequestedBook(requestedBook);

        exchange.setRequester(requester);
        exchange.setOwner(
                requestedBook.getSeller()
        );

        exchange.setMessage(
                request.getMessage()
        );

        exchange.setStatus("PENDING");

        LocalDateTime now =
                LocalDateTime.now();

        exchange.setCreatedAt(now);
        exchange.setUpdatedAt(now);

        Exchange savedExchange = exchangeRepository.save(exchange);

        sendRealtimeNotification(AppNotificationDto.builder()
                .id("notif-" + System.currentTimeMillis())
                .type("EXCHANGE_CREATED")
                .title("New Book Swap Proposal")
                .message(requester.getName() + " proposed to swap \"" + offeredBook.getTitle() + "\" for \"" + requestedBook.getTitle() + "\"")
                .senderId(String.valueOf(requester.getId()))
                .senderName(requester.getName())
                .senderAvatar(requester.getAvatar())
                .targetUserId(String.valueOf(requestedBook.getSeller().getId()))
                .targetUserEmail(requestedBook.getSeller().getEmail())
                .actionUrl("/dashboard/exchanges")
                .time("Just now")
                .timestamp(System.currentTimeMillis())
                .build());

        return toResponse(savedExchange);
    }


    @Transactional(readOnly = true)
    public List<ExchangeResponse>
            getSentExchanges(String email) {

        User user = getUser(email);

        return exchangeRepository
                .findByRequesterOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<ExchangeResponse>
            getReceivedExchanges(String email) {

        User user = getUser(email);

        return exchangeRepository
                .findByOwnerOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @Transactional
    public ExchangeResponse updateStatus(
            Long id,
            ExchangeStatusRequest request,
            String email) {

        User user = getUser(email);

        Exchange exchange =
                exchangeRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Exchange not found"
                    )
                );

        boolean requester =
                exchange.getRequester()
                    .getId()
                    .equals(user.getId());

        boolean owner =
                exchange.getOwner()
                    .getId()
                    .equals(user.getId());

        if (!requester && !owner) {
            throw new RuntimeException(
                "You are not part of this exchange"
            );
        }

        String status =
                request.getStatus()
                    .toUpperCase();

        if (requester &&
            !status.equals("CANCELLED")) {

            throw new RuntimeException(
                "Requester can only cancel"
            );
        }

        if (owner &&
            !(status.equals("ACCEPTED")
              || status.equals("REJECTED"))) {

            throw new RuntimeException(
                "Invalid owner action"
            );
        }

        exchange.setStatus(status);
        exchange.setUpdatedAt(
                LocalDateTime.now()
        );

        Exchange savedExchange = exchangeRepository.save(exchange);

        if ("ACCEPTED".equalsIgnoreCase(status)) {
            sendRealtimeNotification(AppNotificationDto.builder()
                    .id("notif-" + System.currentTimeMillis())
                    .type("EXCHANGE_ACCEPTED")
                    .title("Book Swap Accepted! 🤝")
                    .message(user.getName() + " accepted your swap proposal for \"" + exchange.getRequestedBook().getTitle() + "\"")
                    .senderId(String.valueOf(user.getId()))
                    .senderName(user.getName())
                    .senderAvatar(user.getAvatar())
                    .targetUserId(String.valueOf(exchange.getRequester().getId()))
                    .targetUserEmail(exchange.getRequester().getEmail())
                    .actionUrl("/dashboard/exchanges")
                    .time("Just now")
                    .timestamp(System.currentTimeMillis())
                    .build());
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            sendRealtimeNotification(AppNotificationDto.builder()
                    .id("notif-" + System.currentTimeMillis())
                    .type("EXCHANGE_REJECTED")
                    .title("Book Swap Declined")
                    .message(user.getName() + " declined your swap proposal for \"" + exchange.getRequestedBook().getTitle() + "\"")
                    .senderId(String.valueOf(user.getId()))
                    .senderName(user.getName())
                    .senderAvatar(user.getAvatar())
                    .targetUserId(String.valueOf(exchange.getRequester().getId()))
                    .targetUserEmail(exchange.getRequester().getEmail())
                    .actionUrl("/dashboard/exchanges")
                    .time("Just now")
                    .timestamp(System.currentTimeMillis())
                    .build());
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            sendRealtimeNotification(AppNotificationDto.builder()
                    .id("notif-" + System.currentTimeMillis())
                    .type("EXCHANGE_CANCELLED")
                    .title("Swap Proposal Cancelled")
                    .message(user.getName() + " cancelled their swap proposal for \"" + exchange.getRequestedBook().getTitle() + "\"")
                    .senderId(String.valueOf(user.getId()))
                    .senderName(user.getName())
                    .senderAvatar(user.getAvatar())
                    .targetUserId(String.valueOf(exchange.getOwner().getId()))
                    .targetUserEmail(exchange.getOwner().getEmail())
                    .actionUrl("/dashboard/exchanges")
                    .time("Just now")
                    .timestamp(System.currentTimeMillis())
                    .build());
        }

        return toResponse(savedExchange);
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


    private ExchangeResponse toResponse(
            Exchange exchange) {

        ExchangeResponse response =
                new ExchangeResponse();

        response.setId(exchange.getId());

        response.setOfferedBookId(
                exchange.getOfferedBook().getId()
        );

        response.setOfferedBookTitle(
                exchange.getOfferedBook().getTitle()
        );

        response.setRequestedBookId(
                exchange.getRequestedBook().getId()
        );

        response.setRequestedBookTitle(
                exchange.getRequestedBook().getTitle()
        );

        response.setRequesterId(
                exchange.getRequester().getId()
        );

        response.setRequesterName(
                exchange.getRequester().getName()
        );

        response.setOwnerId(
                exchange.getOwner().getId()
        );

        response.setOwnerName(
                exchange.getOwner().getName()
        );

        response.setMessage(
                exchange.getMessage()
        );

        response.setStatus(
                exchange.getStatus()
        );

        response.setCreatedAt(
                exchange.getCreatedAt()
        );

        response.setUpdatedAt(
                exchange.getUpdatedAt()
        );

        return response;
    }
}