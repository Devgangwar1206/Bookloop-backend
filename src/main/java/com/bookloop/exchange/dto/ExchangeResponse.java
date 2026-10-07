package com.bookloop.exchange.dto;
 
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeResponse {

    private Long id;

    private Long offeredBookId;
    private String offeredBookTitle;

    private Long requestedBookId;
    private String requestedBookTitle;

    private Long requesterId;
    private String requesterName;

    private Long ownerId;
    private String ownerName;

    private String message;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}