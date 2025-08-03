package com.projectshopbando.shopbandoapi.dtos.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreatePaymentUrlReq {
    private String ipAddress;
    private BigDecimal amount;
    private String txnRef;
}
