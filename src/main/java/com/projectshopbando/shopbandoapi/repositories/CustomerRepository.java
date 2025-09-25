package com.projectshopbando.shopbandoapi.repositories;

import com.projectshopbando.shopbandoapi.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    @Query("SELECT c FROM Customer c " +
            "WHERE (:phone IS NULL OR c.phone LIKE CONCAT('%', :phone, '%')) " +
            "ORDER BY c.createdAt DESC")
    Page<Customer> findByPhoneContainingOrderByCreatedAtDesc(String phone, Pageable pageable);

    Optional<Customer> findByPhone(String phone);

    @Query("SELECT c FROM Customer c " +
            "WHERE (:phone IS NULL OR c.phone LIKE CONCAT('%', :phone, '%')) " +
            "AND c.account IS NOT NULL " +
            "ORDER BY c.createdAt DESC")
    Page<Customer> findByPhoneContainingHavingAccount(String phone, Pageable pageable);
}
