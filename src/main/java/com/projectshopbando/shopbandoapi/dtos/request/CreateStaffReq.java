package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CreateStaffReq {

    @NotBlank(message = "Staff full name must not be blank")
    private String fullName;

    @NotBlank(message = "Staff phone number must not be blank")
    private String phone;

    @Email
    @NotBlank(message = "Staff email must not be blank")
    private String email;

    @NotBlank(message = "Staff password must not be blank")
    private String password;

    @NotBlank(message = "Staff position must not be blank")
    private String position;

    @NotBlank(message = "Store ID must not be blank")
    private String store;

    @NotNull(message = "Join date must not be null")
    private LocalDate joinDate;

    @NotNull
    private LocalDate dob;
}
