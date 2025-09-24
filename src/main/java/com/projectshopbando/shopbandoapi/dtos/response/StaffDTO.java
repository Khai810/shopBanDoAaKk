package com.projectshopbando.shopbandoapi.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaffDTO {
    private String id;
    private String fullName;
    private String phone;
    private String position;
    private String store;
    private LocalDate joinDate;
    private LocalDate dob;
    private String email;
    private Set<String> role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
