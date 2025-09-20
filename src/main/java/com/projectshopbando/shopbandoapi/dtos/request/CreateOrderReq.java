package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderReq {

    private String customerId;

    @NotBlank(message = "recipient name must not be blank")
    private String recipientName;

    @NotBlank(message = "recipient phone must not be blank")
    private String recipientPhone;

    @NotBlank(message = "recipient email must not be blank")
    private String recipientEmail;

    @NotBlank(message = "recipient address must not be blank")
    private String recipientAddress;

    @NotNull(message = "Items must not be empty")
    private List<@Valid OrderItemReq> items;

    @NotBlank(message = "Payment method must be not empty")
    private String paymentMethod;

    private String note;
}
