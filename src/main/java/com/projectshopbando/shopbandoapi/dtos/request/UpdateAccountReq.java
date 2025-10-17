package com.projectshopbando.shopbandoapi.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
public class UpdateAccountReq {
    private String email;
    private Set<String> role;
    private LocalDate dob;
}
