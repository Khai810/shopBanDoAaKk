package com.projectshopbando.shopbandoapi.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderStatsDTO {

    private String period;
    private Long totalOrders;
    private BigDecimal totalRevenue;
}
