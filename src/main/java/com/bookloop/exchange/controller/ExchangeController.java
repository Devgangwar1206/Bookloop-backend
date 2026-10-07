package com.bookloop.exchange.controller;
 
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bookloop.exchange.dto.ExchangeCreateRequest;
import com.bookloop.exchange.dto.ExchangeResponse;
import com.bookloop.exchange.dto.ExchangeStatusRequest;
import com.bookloop.exchange.service.ExchangeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/exchanges")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;


    @PostMapping
    public ResponseEntity<ExchangeResponse>
            createExchange(
                @RequestBody ExchangeCreateRequest request,
                Authentication authentication) {

        return ResponseEntity.ok(
            exchangeService.createExchange(
                request,
                authentication.getName()
            )
        );
    }


    @GetMapping("/sent")
    public ResponseEntity<List<ExchangeResponse>>
            getSent(
                Authentication authentication) {

        return ResponseEntity.ok(
            exchangeService.getSentExchanges(
                authentication.getName()
            )
        );
    }


    @GetMapping("/received")
    public ResponseEntity<List<ExchangeResponse>>
            getReceived(
                Authentication authentication) {

        return ResponseEntity.ok(
            exchangeService.getReceivedExchanges(
                authentication.getName()
            )
        );
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<ExchangeResponse>
            updateStatus(
                @PathVariable Long id,
                @RequestBody ExchangeStatusRequest request,
                Authentication authentication) {

        return ResponseEntity.ok(
            exchangeService.updateStatus(
                id,
                request,
                authentication.getName()
            )
        );
    }
}