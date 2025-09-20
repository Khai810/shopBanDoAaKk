package com.projectshopbando.shopbandoapi.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRes {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dob;
    private Set<String> role;
    private BigDecimal points;
}
