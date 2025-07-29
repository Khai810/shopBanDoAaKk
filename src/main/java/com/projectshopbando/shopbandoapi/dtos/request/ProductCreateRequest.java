package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    @NotBlank(message = "Product name must not be empty")
    private String name;

    private String description;

    private List<String> color;

    @NotNull(message = "Product price must not be empty")
    @Positive(message = "Product price must > 0")
    private BigDecimal price;

    @NotNull(message = "In stock must not be empty")
    private Boolean inStock;

    @NotNull(message = "Available must not be empty")
    private Boolean available;

    @NotNull(message = "Category Id must not be empty")
    private Long categoryId;

    @NotBlank(message = "Thumbnail must not be empty")
    private String thumbnail;

    private List<@URL(message = "URL must be valid") String> imageUrls;

    @NotNull(message = "Sizes list must not be null")
    @Size(min = 1, message = "Must contain at least 1 size")
    private List<@Valid ProductSizeDTO> sizes;
}
