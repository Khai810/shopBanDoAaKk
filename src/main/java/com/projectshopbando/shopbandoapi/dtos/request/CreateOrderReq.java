package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderReq {

    private String customerId; // Optional: can be null for guest checkout
    private String staffId; // Optional: can be null for online orders

    @NotBlank(message = "recipient name must not be blank")
    private String name;

    @NotBlank(message = "recipient phone must not be blank")
    private String phone;

    private BigDecimal discount;

    private BigDecimal tax;

    private  BigDecimal shippingFee;

    private String email;

    private String address;

    @NotNull(message = "Items must not be empty")
    private List<@Valid OrderItemReq> items;

    @NotBlank(message = "Payment method must be not empty")
    private String paymentMethod;

    private String note;

    private String type;

}
