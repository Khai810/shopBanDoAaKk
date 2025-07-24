package com.projectshopbando.shopbandoapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_sizes")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductSize {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String size;
    private Integer quantity;

    @ManyToOne()
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
