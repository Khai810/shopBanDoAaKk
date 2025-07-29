package com.projectshopbando.shopbandoapi.dtos.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResPayload {
    private CustomerRes customer;
    private OrderResponse order;
}
