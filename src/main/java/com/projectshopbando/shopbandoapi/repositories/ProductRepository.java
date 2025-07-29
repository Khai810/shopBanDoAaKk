package com.projectshopbando.shopbandoapi.repositories;

import com.projectshopbando.shopbandoapi.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndAvailableTrue(Long id);

    Optional<List<Product>> findTop6ByAvailableTrueOrderByCreatedAtDesc();

    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:excludeId IS NULL OR p.id != :excludeId) AND" +
            "(:name IS NULL OR p.name LIKE %:name%) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "p.available = true")
    Page<Product> searchWithConditions(Long categoryId, Long excludeId, String name
            , BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
