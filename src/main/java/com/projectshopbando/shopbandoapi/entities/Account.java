package com.projectshopbando.shopbandoapi.entities;

import com.projectshopbando.shopbandoapi.enums.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String email;

    private LocalDate dob;

    @NotNull
    private String password;

    @Enumerated(EnumType.STRING)
    private Set<Roles> role;

    @OneToOne()
    @JoinColumn(name = "customer_id", unique = true, nullable = false)
    private Customer customer;
}
