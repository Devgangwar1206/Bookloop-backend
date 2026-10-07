package com.bookloop.order.repository;
 
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookloop.order.entity.Order;
import com.bookloop.user.entity.User;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    List<Order> findByBuyerOrderByCreatedAtDesc(
            User buyer
    );

    List<Order> findBySellerOrderByCreatedAtDesc(
            User seller
    );
}