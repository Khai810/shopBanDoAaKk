package com.projectshopbando.shopbandoapi.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "online_orders")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OnlineOrder extends Order {
    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    private BigDecimal shippingFee;
}