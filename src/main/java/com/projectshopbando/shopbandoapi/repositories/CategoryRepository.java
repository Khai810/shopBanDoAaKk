package com.projectshopbando.shopbandoapi.repositories;

import com.projectshopbando.shopbandoapi.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE (:name IS NULL OR c.name LIKE %:name%) ORDER BY c.updatedAt DESC")
    Page<Category> findByNameContaining(String name, Pageable pageable);

    Optional<Category> findByIdAndIsDisabledFalse(Long id);

    Optional<List<Category>> findAllByIsDisabledFalse();
}
