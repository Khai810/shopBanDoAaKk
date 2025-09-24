package com.projectshopbando.shopbandoapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "staffs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String fullName;

    @NotBlank
    private String phone;

    @NotNull
    private String position;

    @NotBlank
    private String store;

    @NotNull
    private LocalDate joinDate;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne(cascade =  CascadeType.ALL, mappedBy = "staff")
    private Account account;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
