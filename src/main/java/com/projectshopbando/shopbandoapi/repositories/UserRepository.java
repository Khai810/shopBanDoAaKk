package com.projectshopbando.shopbandoapi.repositories;

import com.projectshopbando.shopbandoapi.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Page<User> findByUsernameContainingOrPhoneContainingOrderByCreatedAtDesc(String username, String phone, Pageable pageable);
}
