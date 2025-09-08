package com.projectshopbando.shopbandoapi.dtos.response;

import com.projectshopbando.shopbandoapi.entities.OrderProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class OrderItemsRes {
    List<OrderProduct> orderProducts;
    private BigDecimal totalAmount;
}
