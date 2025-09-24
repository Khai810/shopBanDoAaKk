package com.projectshopbando.shopbandoapi.repositories;

import com.projectshopbando.shopbandoapi.entities.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    boolean existsByCustomer_Phone(String phone);

    Optional<Account> findByCustomer_Id(String id);

    Optional<Account> findByCustomer_Phone(String phone);

    Page<Account> findByCustomer_FullNameContainingOrCustomer_PhoneContainingOrderByCustomer_CreatedAtDesc(String fullName, String phone, Pageable pageable);

    Optional<Account> findByEmail(String email);
}
