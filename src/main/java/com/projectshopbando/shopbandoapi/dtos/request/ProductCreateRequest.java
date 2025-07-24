package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    private String name;
    private String description;
    private List<String> color;
    @NotNull
    private Double price;
    private Boolean inStock;
    private Boolean available;
    private String categoryId;
    private String thumbnail;

    private List<String> imageUrls;
    private List<ProductSizeDTO> sizes;
}
