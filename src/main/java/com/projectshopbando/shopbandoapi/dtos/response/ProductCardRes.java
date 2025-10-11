package com.projectshopbando.shopbandoapi.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCardRes implements Serializable {
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal discountPercent;
    private String thumbnail;
    private Long categoryId;
    private boolean inStock; // New field to indicate stock status
}
