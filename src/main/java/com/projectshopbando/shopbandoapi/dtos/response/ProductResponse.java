package com.projectshopbando.shopbandoapi.dtos.response;

import com.projectshopbando.shopbandoapi.dtos.request.ProductSizeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private List<String> color;
    private BigDecimal price;
    private BigDecimal discountPercent;
    private Boolean inStock;
    private Long categoryId;
    private String thumbnail;

    private List<String> imageUrls;
    private List<ProductSizeDTO> sizes;
}
