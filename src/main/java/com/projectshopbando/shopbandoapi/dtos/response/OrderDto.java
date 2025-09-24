package com.projectshopbando.shopbandoapi.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {
    private String id;
    private String name;
    private String phone;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private int totalQuantity;
    private BigDecimal tax;
    private BigDecimal discount;
    private String paymentMethod;
    private String status;
    private String note;
    //Online Order
    private String email;
    private String address;
    private BigDecimal shippingFee;
    //Offline Order
    private String store;
    private String staffName;
    private List<OrderProductRes> orderedProducts;

}
