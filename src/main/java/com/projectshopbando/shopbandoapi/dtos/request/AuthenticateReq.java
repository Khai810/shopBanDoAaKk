package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticateReq {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
