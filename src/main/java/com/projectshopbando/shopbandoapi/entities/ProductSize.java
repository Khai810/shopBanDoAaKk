package com.projectshopbando.shopbandoapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "product_sizes")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductSize implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String size;
    private Integer quantity;

    @ManyToOne()
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
