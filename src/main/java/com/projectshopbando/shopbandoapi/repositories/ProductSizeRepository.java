package com.projectshopbando.shopbandoapi.repositories;

import com.projectshopbando.shopbandoapi.entities.ProductSize;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") }) // 5 giây
    @Query("SELECT ps FROM ProductSize ps WHERE ps.product.id = :productId AND ps.size = :size AND ps.quantity >= :quantity AND ps.product.available = true")
    Optional<ProductSize> findForUpdate(Long productId, String size, int quantity);

    @Query("SELECT ps FROM ProductSize ps WHERE ps.product.id = :productId AND ps.size = :size AND ps.product.available = true")
    Optional<ProductSize> findByProductIdAndSize(Long productId, String size);


}
