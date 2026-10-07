package com.bookloop.offer.controller;
 
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bookloop.offer.dto.OfferCreateRequest;
import com.bookloop.offer.dto.OfferResponse;
import com.bookloop.offer.dto.OfferStatusRequest;
import com.bookloop.offer.service.OfferService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;


    @PostMapping
    public ResponseEntity<OfferResponse> createOffer(
            @RequestBody OfferCreateRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
            offerService.createOffer(
                request,
                authentication.getName()
            )
        );
    }


    @GetMapping("/sent")
    public ResponseEntity<List<OfferResponse>>
            getSentOffers(
                Authentication authentication) {

        return ResponseEntity.ok(
            offerService.getSentOffers(
                authentication.getName()
            )
        );
    }


    @GetMapping("/received")
    public ResponseEntity<List<OfferResponse>>
            getReceivedOffers(
                Authentication authentication) {

        return ResponseEntity.ok(
            offerService.getReceivedOffers(
                authentication.getName()
            )
        );
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<OfferResponse>
            updateStatus(
                @PathVariable Long id,
                @RequestBody OfferStatusRequest request,
                Authentication authentication) {

        return ResponseEntity.ok(
            offerService.updateStatus(
                id,
                request,
                authentication.getName()
            )
        );
    }
}