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
public class CreateAccountReq {
    @NotBlank(message = "User email must not be blank")
    private String email;
    @Past(message = "DOB_INVALID")
    private LocalDate dob;
    @Size(min = 8, message = "password must at least 8 letters")
    private String password;
}
