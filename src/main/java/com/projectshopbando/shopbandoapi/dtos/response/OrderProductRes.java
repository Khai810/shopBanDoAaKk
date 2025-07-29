package com.projectshopbando.shopbandoapi.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderProductRes {
    private String size;
    private Integer quantity;

    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    private Long productId;
    private String productName;
}
