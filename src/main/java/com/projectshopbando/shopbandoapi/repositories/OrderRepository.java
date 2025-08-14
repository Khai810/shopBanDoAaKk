package com.projectshopbando.shopbandoapi.repositories;

import com.projectshopbando.shopbandoapi.entities.Order;
import com.projectshopbando.shopbandoapi.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status =:status) AND "+
            "(:search IS NULL OR o.recipientEmail LIKE %:search% OR o.recipientPhone LIKE %:search%)" +
            "ORDER BY o.orderDate DESC")
    Page<Order> adminFindAll(String search, OrderStatus status, Pageable pageable);

    List<Order> findAllByCustomerIdOrderByOrderDateDesc(String customerId);
}
