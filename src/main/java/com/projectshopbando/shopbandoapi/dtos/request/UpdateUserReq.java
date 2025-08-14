package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserReq {
    @NotBlank(message = "User full name must not be blank")
    private String fullName;
    @NotBlank(message = "User email must not be blank")
    private String email;
    @NotBlank(message = "User phone number must not be blank")
    @Size(min = 10, message = "Phone number must be at least 10 digits")
    private String phone;
    @Past(message = "Date of birth invalid")
    private LocalDate dob;
}
