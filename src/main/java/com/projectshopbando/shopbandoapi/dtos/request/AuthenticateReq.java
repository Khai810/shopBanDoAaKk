package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticateReq {
    @NotBlank
    private String phone;
    @NotBlank
    private String password;
}
