package com.projectshopbando.shopbandoapi;

import com.projectshopbando.shopbandoapi.config.VnPayConfig;
import com.projectshopbando.shopbandoapi.entities.Order;
import com.projectshopbando.shopbandoapi.enums.OrderStatus;
import com.projectshopbando.shopbandoapi.enums.PaymentMethod;
import com.projectshopbando.shopbandoapi.services.OrderService;
import com.projectshopbando.shopbandoapi.services.VNPayIpnHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VNPayIpnHandlerTest {
    @Mock
    private OrderService orderService;

    @InjectMocks
    private VNPayIpnHandler vnPayIpnHandler;

    private Order order;

    private Map<String, String> params;
    @BeforeEach
    public void setup() {
        this.order = Order.builder()
                .id("testOrder")
                .orderedProduct(List.of())
                .status(OrderStatus.UNPAID)
                .paymentMethod(PaymentMethod.VNPAY)
                .totalAmount(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_UP))
                .build();
        this.params = new HashMap<>(Map.of(
                "vnp_SecureHash", "validHash",
                "vnp_SecureHashType", "SHA256",
                "vnp_ResponseCode", "00",
                "vnp_TxnRef", "testOrder",
                "vnp_Amount", "1000000"
        ));

    }

    @org.junit.jupiter.api.Order(1)
    @Test
    public void testIpnHandler_Success() {
        try (MockedStatic<VnPayConfig> mockedStatic = mockStatic(VnPayConfig.class)) {
            mockedStatic.when(() -> VnPayConfig.hmacSHA512(anyString(), anyString()))
                    .thenReturn("validHash");
            when(orderService.checkIfOrderExists(anyString())).thenReturn(true);
            when(orderService.getOrderById(anyString())).thenReturn(this.order);
            doNothing().when(orderService).updateOrderStatus(any(Order.class), any(OrderStatus.class));
            var response = vnPayIpnHandler.ipnHandler(this.params);
            assertThat(response.getRspCode()).isEqualTo("00");
            verify(orderService).updateOrderStatus(this.order, OrderStatus.PREPARING);
        }
    }

    @org.junit.jupiter.api.Order(2)
    @Test
    public void testIpnHandler_InvalidCheckSum() {
        try (MockedStatic<VnPayConfig> mockedStatic = mockStatic(VnPayConfig.class)) {
            mockedStatic.when(() -> VnPayConfig.hmacSHA512(anyString(), anyString()))
                    .thenReturn("invalid Hash");
            var response = vnPayIpnHandler.ipnHandler(this.params);
            assertThat(response.getRspCode()).isEqualTo("97");
            verify(orderService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
        }
    }

    @org.junit.jupiter.api.Order(3)
    @Test
    public void testIpnHandler_OrderNotFound() {
        try (MockedStatic<VnPayConfig> mockedStatic = mockStatic(VnPayConfig.class)) {
            mockedStatic.when(() -> VnPayConfig.hmacSHA512(anyString(), anyString()))
                    .thenReturn("validHash");
            when(orderService.checkIfOrderExists(anyString())).thenReturn(false);
            var response = vnPayIpnHandler.ipnHandler(this.params);
            assertThat(response.getRspCode()).isEqualTo("01");
            verify(orderService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
        }
    }

    @org.junit.jupiter.api.Order(4)
    @Test
    public void testIpnHandler_InvalidAmount() {
        try (MockedStatic<VnPayConfig> mockedStatic = mockStatic(VnPayConfig.class)) {
            mockedStatic.when(() -> VnPayConfig.hmacSHA512(anyString(), anyString()))
                    .thenReturn("validHash");
            when(orderService.checkIfOrderExists(anyString())).thenReturn(true);
            when(orderService.getOrderById(anyString())).thenReturn(this.order);
            this.params.put("vnp_Amount", "2000000");
            var response = vnPayIpnHandler.ipnHandler(this.params);
            assertThat(response.getRspCode()).isEqualTo("04");
            verify(orderService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
        }
    }

    @org.junit.jupiter.api.Order(5)
    @Test
    public void testIpnHandler_OrderConfirmedAlready() {
        try (MockedStatic<VnPayConfig> mockedStatic = mockStatic(VnPayConfig.class)) {
            mockedStatic.when(() -> VnPayConfig.hmacSHA512(anyString(), anyString()))
                    .thenReturn("validHash");
            this.order.setStatus(OrderStatus.PREPARING);
            when(orderService.checkIfOrderExists(anyString())).thenReturn(true);
            when(orderService.getOrderById(anyString())).thenReturn(this.order);
            var response = vnPayIpnHandler.ipnHandler(this.params);
            assertThat(response.getRspCode()).isEqualTo("02");
            verify(orderService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
        }
    }
}
