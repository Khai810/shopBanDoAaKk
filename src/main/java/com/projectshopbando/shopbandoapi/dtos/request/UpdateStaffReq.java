package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class UpdateStaffReq {

    @NotBlank(message = "Staff full name must not be blank")
    private String fullName;

    @NotBlank(message = "Staff phone number must not be blank")
    private String phone;

    @Email
    @NotBlank(message = "Staff email must not be blank")
    private String email;

    @NotBlank(message = "Staff position must not be blank")
    private String position;

    @NotBlank(message = "Store ID must not be blank")
    private String store;

    private LocalDate joinDate;

    private Set<String> role;

    private LocalDate dob;
}
