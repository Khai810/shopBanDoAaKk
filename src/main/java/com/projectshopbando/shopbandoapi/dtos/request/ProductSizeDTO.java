package com.projectshopbando.shopbandoapi.dtos.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSizeDTO {
    private String size;
    private Integer quantity;
}
