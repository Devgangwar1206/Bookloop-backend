package com.bookloop.offer.dto;
 
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferCreateRequest {

    private Long bookId;

    private BigDecimal amount;

    private String message;

    private Long parentOfferId;
}