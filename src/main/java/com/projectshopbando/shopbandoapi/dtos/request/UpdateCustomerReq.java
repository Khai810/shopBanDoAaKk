package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UpdateCustomerReq {
    @NotBlank(message = "User full name must not be blank")
    private String fullName;
    @NotBlank(message = "User email must not be blank")
    private String email;
    @Past(message = "Date of birth invalid")
    private LocalDate dob;
}
