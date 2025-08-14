package com.projectshopbando.shopbandoapi.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerReq {
    @NotBlank(message = "Customer last name must not be blank")
    private String fullName;
    @NotBlank(message = "Customer email must not be blank")
    private String email;
    @NotBlank(message = "Customer address must not be blank")
    private String address;
    @NotBlank(message = "Customer phone number must not be blank")
    @Size(min = 10, message = "Phone number must be at least 10 digits")
    private String phone;

}
