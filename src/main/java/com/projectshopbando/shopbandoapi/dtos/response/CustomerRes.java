package com.projectshopbando.shopbandoapi.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRes {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
}
