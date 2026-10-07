package com.bookloop.order.controller;

 
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bookloop.order.dto.OrderCreateRequest;
import com.bookloop.order.dto.OrderResponse;
import com.bookloop.order.dto.OrderStatusRequest;
import com.bookloop.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping
    public ResponseEntity<OrderResponse>
            createOrder(
                @RequestBody OrderCreateRequest request,
                Authentication authentication) {

        return ResponseEntity.ok(
            orderService.createOrder(
                request,
                authentication.getName()
            )
        );
    }


    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>>
            getMyOrders(
                Authentication authentication) {

        return ResponseEntity.ok(
            orderService.getMyOrders(
                authentication.getName()
            )
        );
    }


    @GetMapping("/seller")
    public ResponseEntity<List<OrderResponse>>
            getSellerOrders(
                Authentication authentication) {

        return ResponseEntity.ok(
            orderService.getSellerOrders(
                authentication.getName()
            )
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse>
            getOrder(
                @PathVariable Long id,
                Authentication authentication) {

        return ResponseEntity.ok(
            orderService.getOrder(
                id,
                authentication.getName()
            )
        );
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse>
            updateStatus(
                @PathVariable Long id,
                @RequestBody OrderStatusRequest request,
                Authentication authentication) {

        return ResponseEntity.ok(
            orderService.updateStatus(
                id,
                request,
                authentication.getName()
            )
        );
    }


    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse>
            cancelOrder(
                @PathVariable Long id,
                Authentication authentication) {

        return ResponseEntity.ok(
            orderService.cancelOrder(
                id,
                authentication.getName()
            )
        );
    }
}