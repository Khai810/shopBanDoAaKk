package com.projectshopbando.shopbandoapi.dtos.request;

import lombok.Data;

@Data
public class AuthenticateReq {
    private String username;
    private String password;
}
