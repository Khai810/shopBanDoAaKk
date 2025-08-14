package com.projectshopbando.shopbandoapi.entities;

import com.projectshopbando.shopbandoapi.enums.Roles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "users")
@DiscriminatorValue("User")
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends Customer {

    private LocalDate dob;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Set<Roles> role;
}
