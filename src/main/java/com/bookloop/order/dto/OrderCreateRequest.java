package com.bookloop.order.dto;
 
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreateRequest {

    private Long bookId;

    private String deliveryAddress;
}