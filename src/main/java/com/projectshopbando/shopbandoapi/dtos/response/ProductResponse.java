package com.projectshopbando.shopbandoapi.dtos.response;

import com.projectshopbando.shopbandoapi.dtos.request.ProductSizeDTO;
import jakarta.validation.constraints.NotNull;
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
    private String id;
    private String name;
    private String description;
    private List<String> color;
    @NotNull
    private BigDecimal price;
    private Boolean inStock;
    private Boolean available;
    private String categoryId;
    private String thumbnail;

    private List<String> imageUrls;
    private List<ProductSizeDTO> sizes;
}
