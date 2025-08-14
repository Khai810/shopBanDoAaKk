package com.projectshopbando.shopbandoapi.dtos.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserRes {
    private String id;
    private String fullName;
    private String phone;
    private String email;
    private Set<String> role;
    private String username;
    private LocalDate dob;
}
