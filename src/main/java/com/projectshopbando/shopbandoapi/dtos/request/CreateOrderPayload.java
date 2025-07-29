package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderPayload {
    @NotNull(message = "Customer information must not be empty")
    private @Valid CreateCustomerReq customerReq;
    @NotNull(message = "Order list must not be empty")
    private @Valid CreateOrderReq orderReq;
}
