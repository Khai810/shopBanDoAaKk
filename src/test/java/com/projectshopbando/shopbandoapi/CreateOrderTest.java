package com.projectshopbando.shopbandoapi;

import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreatePaymentUrlReq;
import com.projectshopbando.shopbandoapi.dtos.request.OrderItemReq;
import com.projectshopbando.shopbandoapi.dtos.response.OrderResponse;
import com.projectshopbando.shopbandoapi.dtos.response.PaymentResponse;
import com.projectshopbando.shopbandoapi.entities.Customer;
import com.projectshopbando.shopbandoapi.entities.Order;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.entities.Staff;
import com.projectshopbando.shopbandoapi.enums.OrderStatus;
import com.projectshopbando.shopbandoapi.mappers.OrderMapper;
import com.projectshopbando.shopbandoapi.repositories.OrderRepository;
import com.projectshopbando.shopbandoapi.services.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateOrderTest {
    @Mock private CustomerService customerService;
    @Mock private StaffService staffService;
    @Mock private ProductService productService;
    @Mock private PaymentService paymentService;
    @Mock private OrderRepository orderRepository;
    @Mock private EmailSenderService emailSenderService;
    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);
    @Mock private HttpServletRequest httpRequest;

    private OrderService orderService; // class chứa createOrder

    private Customer customer;
    private Staff staff;
    private Product product;
    private BigDecimal productPrice = BigDecimal.valueOf(100000);
    private BigDecimal discountPercent = BigDecimal.valueOf(10);
    private Integer orderQuantity = 2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this); // ✅ Mở mock trước

        orderService = new OrderService(
                orderRepository,
                customerService,
                productService,
                paymentService,
                orderMapper, // mapper thật
                staffService,
                emailSenderService
        );

        customer = Customer.builder()
                .id("cust123")
                .fullName("Khai Pham")
                .phone("0909999999")
                .build();

        staff = Staff.builder()
                .id("staff001")
                .fullName("Staff Test")
                .store("TestStore")
                .build();

        product = Product.builder()
                .id(1L)
                .name("Sample Product")
                .price(productPrice)
                .discountPercent(discountPercent)
                .build();
    }


    @AfterEach
    public void cleanup() {
        // Additional cleanup if needed
        log.info("Test cleanup completed");
    }

    @Test
    void testCreateOnlineOrder_VNPay_WithCustomerID_Success() throws Exception {
        // Mock request
        OrderItemReq item = new OrderItemReq();
        item.setProductId(Long.valueOf(1));
        item.setQuantity(orderQuantity);
        item.setSize("M");

        CreateOrderReq req = CreateOrderReq.builder()
                .customerId("cust123")
                .email("test@gmail.com")
                .address("123 Street")
                .items(List.of(item))
                .paymentMethod("VNPAY")
                .name("recipientName")
                .phone("0909999999")
                .totalAmount(BigDecimal.valueOf(100000 * 0.9 * orderQuantity)) // 10% discount
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .note("note")
                .type("ONLINE")
                .build();

        when(customerService.getCustomerById("cust123")).thenReturn(customer);
        when(productService.getProductByIdAndAvailableTrue(Long.valueOf(1))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentService.initPaymentUrl(any(CreatePaymentUrlReq.class)))
                .thenReturn(new PaymentResponse("https://sandbox.vnpay.vn/pay"));

        // Gọi service
        OrderResponse response = orderService.createOrder(req, httpRequest);

        // ✅ Kiểm tra
        assertThat(response).isNotNull();
        assertThat(response.getPayment().getPaymentUrl()).contains("vnpay");
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(emailSenderService, never()).sendOrderConfirmationEmail(any());
        verify(paymentService).initPaymentUrl(any(CreatePaymentUrlReq.class));
    }

    @Test
    void testCreateOnlineOrder_COD_WithNoCustomerID_Success() throws Exception {
        OrderItemReq item = new OrderItemReq();
        item.setProductId(Long.valueOf(1));
        item.setQuantity(orderQuantity);
        item.setSize("L");

        CreateOrderReq req = CreateOrderReq.builder()
                .email("test@gmail.com")
                .address("123 Street")
                .items(List.of(item))
                .paymentMethod("COD")
                .name("recipientName")
                .phone("0909999999")
                .totalAmount(BigDecimal.valueOf(100000 * 0.9 * orderQuantity)) // 10% discount
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .note("note")
                .type("ONLINE")
                .build();

        when(customerService.createCustomer(any())).thenReturn(customer);
        when(productService.getProductByIdAndAvailableTrue(Long.valueOf(1))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.createOrder(req, httpRequest);

        // ✅ Assert
        assertThat(response.getPayment()).isNull();
        verify(emailSenderService, times(1)).sendOrderConfirmationEmail(any(Order.class));
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void testCreateOfflineOrder_CASH_WithCustomerId_Success() throws Exception {
        OrderItemReq item = new OrderItemReq();
        item.setProductId(Long.valueOf(1));
        item.setQuantity(orderQuantity);
        item.setSize("XL");

        CreateOrderReq req = CreateOrderReq.builder()
                .customerId("cust123")
                .staffId("staff001")
                .items(List.of(item))
                .paymentMethod("CASH")
                .name("recipientName")
                .phone("0909999999")
                .totalAmount(BigDecimal.valueOf(100000 * 0.9 * orderQuantity)) // 10% discount
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .note("note")
                .type("OFFLINE")
                .build();

        when(customerService.getCustomerById("cust123")).thenReturn(customer);
        when(staffService.getStaffById("staff001")).thenReturn(staff);
        when(productService.getProductByIdAndAvailableTrue(Long.valueOf(1))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.createOrder(req, httpRequest);

        // ✅ Assert
        assertThat(response.getPayment()).isNull();
        assertThat(response.getOrder().getStatus()).isEqualTo(OrderStatus.COMPLETED.toString());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(emailSenderService, never()).sendOrderConfirmationEmail(any());
    }

    @Test
    void testCreateOrder_InvalidTotalAmount_ThrowsException() {
        OrderItemReq item = new OrderItemReq();
        item.setProductId(Long.valueOf(1));
        item.setQuantity(orderQuantity);
        item.setSize("M");

        CreateOrderReq req = CreateOrderReq.builder()
                .customerId("cust123")
                .items(List.of(item))
                .totalAmount(BigDecimal.valueOf(50000)) // sai intentionally
                .paymentMethod("COD")
                .email("test@gmail.com")
                .address("123 Street")
                .name("recipientName")
                .phone("0909999999")
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .note("note")
                .type("ONLINE")
                .build();

        when(customerService.getCustomerById("cust123")).thenReturn(customer);
        when(productService.getProductByIdAndAvailableTrue(Long.valueOf(1))).thenReturn(product);

        assertThatThrownBy(() -> orderService.createOrder(req, httpRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Total amount doesn't match");
    }



}