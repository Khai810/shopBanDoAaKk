package com.projectshopbando.shopbandoapi.entities;

import com.projectshopbando.shopbandoapi.enums.OrderStatus;
import com.projectshopbando.shopbandoapi.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String phone;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    private String note;

    private BigDecimal tax = BigDecimal.ZERO;

    private BigDecimal discount = BigDecimal.ZERO;

    private int totalQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderedProducts  = new ArrayList<>();

    @ManyToOne()
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
    }
}
