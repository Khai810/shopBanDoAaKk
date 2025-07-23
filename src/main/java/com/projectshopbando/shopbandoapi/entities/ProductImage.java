package com.projectshopbando.shopbandoapi.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String url;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
