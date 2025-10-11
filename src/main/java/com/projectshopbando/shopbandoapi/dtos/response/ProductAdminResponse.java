package com.projectshopbando.shopbandoapi.dtos.response;

import com.projectshopbando.shopbandoapi.dtos.request.ProductSizeDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductAdminResponse {
    private Long id;
    private String name;
    private String description;
    private List<String> color;
    private BigDecimal price;
    private BigDecimal discountPercent;
    private Boolean inStock;
    private Long categoryId;
    private String thumbnail;
    private Boolean available;

    private List<String> imageUrls;
    private List<ProductSizeDTO> sizes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
