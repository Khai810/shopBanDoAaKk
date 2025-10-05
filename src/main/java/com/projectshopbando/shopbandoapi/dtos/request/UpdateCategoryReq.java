package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCategoryReq {

    @NotBlank(message = "Category name must not empty")
    private String name;

    @NotBlank(message = "Image URL must not empty")
    private String imageUrl;

    @NotBlank(message = "Description must not empty")
    private String description;

    private Boolean isDisabled;
}
