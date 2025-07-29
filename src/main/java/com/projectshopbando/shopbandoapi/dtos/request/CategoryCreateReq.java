package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateReq {
    @NotBlank(message = "Category name must not empty")
    private String name;
}
