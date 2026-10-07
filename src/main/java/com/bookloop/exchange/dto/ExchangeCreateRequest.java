 
package com.bookloop.exchange.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeCreateRequest {

    private Long offeredBookId;

    private Long requestedBookId;

    private String message;
}