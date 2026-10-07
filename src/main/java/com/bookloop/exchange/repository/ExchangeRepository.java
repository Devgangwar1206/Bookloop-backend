package com.bookloop.exchange.repository;
 
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookloop.exchange.entity.Exchange;
import com.bookloop.user.entity.User;

public interface ExchangeRepository
        extends JpaRepository<Exchange, Long> {

    List<Exchange> findByRequesterOrderByCreatedAtDesc(
            User requester
    );

    List<Exchange> findByOwnerOrderByCreatedAtDesc(
            User owner
    );
}