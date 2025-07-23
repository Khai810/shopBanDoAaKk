package com.projectshopbando.shopbandoapi.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "product_sizes")
public class ProductSize {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String size;
    private String quantity;

    @ManyToOne()
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
