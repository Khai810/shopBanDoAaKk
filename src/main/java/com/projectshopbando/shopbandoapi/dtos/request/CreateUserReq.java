package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserReq {
    @NotBlank(message = "User last name must not be blank")
    private String fullName;
    @NotBlank(message = "User email must not be blank")
    private String email;
    @NotBlank(message = "User phone number must not be blank")
    @Size(min = 10, message = "Phone number must be at least 10 digits")
    private String phone;
    @Past(message = "DOB_INVALID")
    private LocalDate dob;

    @Size(min = 5, message = "username must at least 5 letters")
    private String username;
    @Size(min = 8, message = "password must at least 8 letters")
    private String password;
}
