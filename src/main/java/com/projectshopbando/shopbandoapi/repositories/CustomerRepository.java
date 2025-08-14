package com.projectshopbando.shopbandoapi.repositories;

import com.projectshopbando.shopbandoapi.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    @Query("SELECT c FROM Customer c WHERE TYPE(c) = Customer AND " +
            "(:phone IS NULL OR c.phone LIKE %:phone%) ORDER BY c.createdAt DESC")
    Page<Customer> findByPhoneContainingOrderByCreatedAtDesc(String phone, Pageable pageable);
}
