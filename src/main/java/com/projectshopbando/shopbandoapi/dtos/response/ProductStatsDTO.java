package com.projectshopbando.shopbandoapi.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatsDTO {
    private Long id;
    private String name;
    private BigDecimal totalRevenue;
    private Integer totalUnitsSold;
}
