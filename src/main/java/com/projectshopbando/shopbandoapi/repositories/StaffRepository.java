package com.projectshopbando.shopbandoapi.repositories;

import com.projectshopbando.shopbandoapi.entities.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;


public interface StaffRepository extends JpaRepository<Staff, String> {
    boolean existsByAccount_Email(String email);

    @Query("SELECT s FROM Staff s WHERE " +
            "s.fullName LIKE %:search% OR " +
            "s.phone LIKE %:search% OR " +
            "s.account.email LIKE %:search% " +
            "ORDER BY s.updatedAt DESC")
    Page<Staff> findAllStaffWithSearch(String search, Pageable pageable);
}
