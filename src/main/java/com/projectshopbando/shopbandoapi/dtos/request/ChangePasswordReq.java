package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordReq {
    @Size(min = 8, message = "PASSWORD_INVALID")
    private String oldPassword;
    @Size(min = 8, message = "PASSWORD_INVALID")
    private String newPassword;
    @Size(min = 8, message = "PASSWORD_INVALID")
    private String confirmPassword;
}
