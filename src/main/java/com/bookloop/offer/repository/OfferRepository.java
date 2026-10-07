package com.bookloop.offer.repository;
 
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookloop.offer.entity.Offer;
import com.bookloop.user.entity.User;

public interface OfferRepository
        extends JpaRepository<Offer, Long> {

    List<Offer> findByBuyerOrderByCreatedAtDesc(
            User buyer
    );

    List<Offer> findBySellerOrderByCreatedAtDesc(
            User seller
    );
}