package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemReq {
    @NotNull(message = "productId must not be empty")
    private Long productId;
    @NotBlank(message = "Product size must not be empty")
    private String size;
    @NotNull(message = "quantity must not be empty")
    private Integer quantity;
}
