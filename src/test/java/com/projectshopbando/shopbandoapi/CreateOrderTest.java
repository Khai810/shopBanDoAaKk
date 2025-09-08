package com.projectshopbando.shopbandoapi;

import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.request.OrderItemReq;
import com.projectshopbando.shopbandoapi.dtos.response.OrderResponse;
import com.projectshopbando.shopbandoapi.entities.*;
import com.projectshopbando.shopbandoapi.enums.OrderStatus;
import com.projectshopbando.shopbandoapi.enums.PaymentMethod;
import com.projectshopbando.shopbandoapi.exception.NotFoundException;
import com.projectshopbando.shopbandoapi.repositories.*;
import com.projectshopbando.shopbandoapi.services.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateOrderTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Product testProduct;
    private static final int INITIAL_STOCK = 20;
    private static final int ORDER_QUANTITY = 2;

    @BeforeEach
    public void setUpProductWithStock() {
        orderRepository.deleteAll();
        productSizeRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = Category.builder()
                .name("testCategory")
                .description("testCateDescription")
                .build();
        category = categoryRepository.save(category);

        Product testProduct = Product.builder()
                .name("testProduct")
                .price(BigDecimal.valueOf(300000.00))
                .inStock(true)
                .available(true)
                .category(category)
                .build();
        this.testProduct = productRepository.save(testProduct);

        ProductSize testProductSize = ProductSize.builder()
                .product(testProduct)
                .size("M")
                .quantity(INITIAL_STOCK)
                .build();
        productSizeRepository.save(testProductSize);

        User testUser = User.builder()
                .fullName("testUser")
                .phone("testPhone")
                .address("testAddress")
                .email("testEmail")
                .dob(LocalDate.of(2000, 1, 1))
                .password("testPassword")
                .username("testUsername")
                .build();
        this.testUser = userRepository.save(testUser);

        log.info("testUser = {}", this.testUser.getId());
        log.info("Setup completed. Product ID: {}, Initial stock: {}", testProduct.getId(), INITIAL_STOCK);

    }

    @AfterEach
    public void cleanup() {
        // Additional cleanup if needed
        log.info("Test cleanup completed");
    }

    @Test
    @Order(1)
    @DisplayName("Should create order with existing user")
    public void testCreateOrderWithUserId_Success() throws BadRequestException {
        CreateOrderReq orderReq = CreateOrderReq.builder()
                .recipientAddress("ADDRESS")
                .recipientPhone("0123123123")
                .userId(this.testUser.getId())
                .paymentMethod(PaymentMethod.COD.toString())
                .recipientEmail("EMAIL")
                .recipientName("NAME")
                .items(List.of(new OrderItemReq(this.testProduct.getId(), "M", 1)))
                .note("NOTE")
                .build();
        HttpServletRequest httpReq = new MockHttpServletRequest();

        OrderResponse response = orderService.createOrder(orderReq, httpReq);

        assertThat(response).isNotNull();
        assertThat(orderRepository.findById(response.getOrder().getId()).get().getCustomer().getId()).isEqualTo(this.testUser.getId());
        assertThat(response.getOrder().getRecipientName()).isEqualTo("NAME");
        assertThat(response.getOrder().getTotalAmount().compareTo(this.testProduct.getPrice())).isZero();
        assertThat(response.getOrder().getStatus()).isEqualTo(OrderStatus.PREPARING.toString());
    }

    @Test
    @Order(2)
    @DisplayName("Should create order with no user needed")
    public void testCreateOrderWithoutUserId_Success() throws BadRequestException {
        CreateOrderReq orderReq = CreateOrderReq.builder()
                .recipientAddress("ADDRESS")
                .recipientPhone("0123123123")
                .userId(null)
                .paymentMethod(PaymentMethod.COD.toString())
                .recipientEmail("EMAIL")
                .recipientName("NAME")
                .items(List.of(new OrderItemReq(this.testProduct.getId(), "M", 1)))
                .note("NOTE")
                .build();
        HttpServletRequest httpReq = new MockHttpServletRequest();

        OrderResponse response = orderService.createOrder(orderReq, httpReq);

        assertThat(response).isNotNull();
        assertThat(response.getOrder().getRecipientName()).isEqualTo("NAME");
        assertThat(response.getOrder().getTotalAmount().compareTo(this.testProduct.getPrice())).isZero();
        assertThat(response.getOrder().getStatus()).isEqualTo(OrderStatus.PREPARING.toString());
    }

    @Test
    @Order(3)
    @DisplayName("Should have VNPAY payment url")
    void testCreateOrder_VNPAYPaymentUrl() throws BadRequestException {
        CreateOrderReq orderReq = CreateOrderReq.builder()
                .recipientAddress("ADDRESS")
                .recipientPhone("0123123123")
                .userId(null)
                .paymentMethod(PaymentMethod.VNPAY.toString())
                .recipientEmail("EMAIL")
                .recipientName("NAME")
                .items(List.of(new OrderItemReq(this.testProduct.getId(), "M", 1)))
                .note("NOTE")
                .build();

        OrderResponse response = orderService.createOrder(orderReq, new MockHttpServletRequest());
        assertThat(response).isNotNull();
        assertThat(response.getPayment().getPaymentUrl()).isNotNull();
        assertThat(response.getOrder().getStatus()).isEqualTo(OrderStatus.UNPAID.toString());
    }

    @Test
    @Order(4)
    @DisplayName("Should handle concurrent order creation requests correctly")
    public void testOrderConcurrency() throws InterruptedException {
        // Given
        int numberOfThreads = 13; // Reduced to avoid overwhelming the system
        int requestsPerThread = 1;
        int totalExpectedOrders = numberOfThreads * requestsPerThread;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1); // All threads start together
        CountDownLatch completeLatch = new CountDownLatch(numberOfThreads);

        // Thread-safe collections for results
        List<String> successfulOrders = Collections.synchronizedList(new ArrayList<>());
        List<String> failedOrders = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // When - Submit concurrent order creation tasks
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await(10, TimeUnit.SECONDS);

                    CreateOrderReq orderReq = new CreateOrderReq(
                            null,
                            "Customer" + threadIndex,
                            "012312312" + threadIndex,
                            threadIndex + "@mail.com",
                            "Address " + threadIndex,
                            List.of(new OrderItemReq(testProduct.getId(), "M", ORDER_QUANTITY)),
                            "COD",
                            "Note " + threadIndex
                    );

                    HttpServletRequest request = new MockHttpServletRequest();

                    // This should be atomic due to database-level locking
                    OrderResponse orderResponse = orderService.createOrder(orderReq, request);

                    String orderId = orderResponse.getOrder().getId();
                    successfulOrders.add(orderId);
                    successCount.incrementAndGet();

                    log.info("✅ Thread {} - Order created successfully: {}", threadIndex, orderId);

                } catch (Exception e) {
                    failedOrders.add("Thread " + threadIndex + ": " + e.getMessage());
                    failureCount.incrementAndGet();
                    log.error("❌ Thread {} - Order creation failed: {}", threadIndex, e.getMessage());
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete (with timeout)
        boolean allCompleted = completeLatch.await(30, TimeUnit.SECONDS);
        assertTrue(allCompleted, "All threads should complete within timeout");

        executor.shutdown();
        boolean terminated = executor.awaitTermination(10, TimeUnit.SECONDS);
        assertTrue(terminated, "Executor should terminate gracefully");

        // Then - Verify results
        log.info("=== TEST RESULTS ===");
        log.info("Successful orders: {}", successCount.get());
        log.info("Failed orders: {}", failureCount.get());
        log.info("Total requests: {}", numberOfThreads);

        // Print details
        successfulOrders.forEach(orderId -> log.info("✅ Created order: {}", orderId));
        failedOrders.forEach(error -> log.info("❌ Failed: {}", error));

        // Verify final stock quantity
        ProductSize finalProductSize = productSizeRepository
                .findByProductIdAndSize(testProduct.getId(), "M")
                .orElseThrow(() -> new AssertionError("ProductSize should exist"));

        int finalStock = finalProductSize.getQuantity();
        int expectedFinalStock = INITIAL_STOCK - (successCount.get() * ORDER_QUANTITY);

        log.info("Initial stock: {}, Final stock: {}, Expected final stock: {}",
                INITIAL_STOCK, finalStock, expectedFinalStock);

        // Assertions
        assertEquals(expectedFinalStock, finalStock,
                "Final stock should equal initial stock minus successful orders");

        // Verify that we either got all successes or some failures due to stock exhaustion
        assertTrue(successCount.get() + failureCount.get() == numberOfThreads,
                "All requests should either succeed or fail");

        // If we expect stock exhaustion, verify it happens correctly
        if (totalExpectedOrders > INITIAL_STOCK) {
            assertTrue(failureCount.get() > 0,
                    "Should have some failures when trying to order more than available stock");
            assertTrue(successCount.get() <= INITIAL_STOCK / ORDER_QUANTITY,
                    "Successful orders should not exceed available stock");
        }

        // Verify no duplicate order IDs
        assertEquals(successfulOrders.size(), successfulOrders.stream().distinct().count(),
                "All successful order IDs should be unique");
    }

    @Test
    @Order(5)
    @DisplayName("Should throw exception when product not found")
    void testCreateOrder_ProductNotFound() {
        // Given
        CreateOrderReq orderReq = CreateOrderReq.builder()
                .recipientAddress("ADDRESS")
                .recipientPhone("0123123123")
                .userId(null)
                .paymentMethod(PaymentMethod.COD.toString())
                .recipientEmail("EMAIL")
                .recipientName("NAME")
                .items(List.of(new OrderItemReq(999L, "M", 1))) // Non-existent product
                .note("NOTE")
                .build();

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(orderReq, new MockHttpServletRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found with id: " + 999L);
    }

    @Test
    @Order(6)
    @DisplayName("Should throw exception when product size not found")
    void testCreateOrder_ProductSizeNotFound() {
        // Given
        CreateOrderReq orderReq = CreateOrderReq.builder()
                .recipientAddress("ADDRESS")
                .recipientPhone("0123123123")
                .userId(null)
                .paymentMethod(PaymentMethod.COD.toString())
                .recipientEmail("EMAIL")
                .recipientName("NAME")
                .items(List.of(new OrderItemReq(this.testProduct.getId(), "NOSIZEFOUND", 1))) // Non-existent product
                .note("NOTE")
                .build(); // Size doesn't exist

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(orderReq, new MockHttpServletRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Not enough stock for size NOSIZEFOUND");
    }

    @Test
    @Order(7)
    @DisplayName("Should throw exception when insufficient stock")
    void testCreateOrder_InsufficientStock() {
        CreateOrderReq orderReq = CreateOrderReq.builder()
                .recipientAddress("ADDRESS")
                .recipientPhone("0123123123")
                .userId(null)
                .paymentMethod(PaymentMethod.COD.toString())
                .recipientEmail("EMAIL")
                .recipientName("NAME")
                .items(List.of(new OrderItemReq(this.testProduct.getId(), "M", INITIAL_STOCK + 10))) // Non-existent product
                .note("NOTE")
                .build();

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(orderReq, new MockHttpServletRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Not enough stock for size M");
    }

    @Test
    @Order(8)
    @DisplayName("Should throw exception for invalid payment method")
    void testCreateOrder_InvalidPaymentMethod() {
        // Given
        CreateOrderReq orderReq = CreateOrderReq.builder()
                .recipientAddress("ADDRESS")
                .recipientPhone("0123123123")
                .userId(null)
                .paymentMethod("INVALID_METHOD") // Invalid payment method
                .recipientEmail("EMAIL")
                .recipientName("NAME")
                .items(List.of(new OrderItemReq(this.testProduct.getId(), "M", 1)))
                .note("NOTE")
                .build();

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(orderReq, new MockHttpServletRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid payment method");
    }

    @Test
    @Order(9)
    @DisplayName("Should handle negative quantity")
    void testCreateOrder_NegativeQuantity() {
        // Given
        CreateOrderReq orderReq = CreateOrderReq.builder()
                .recipientAddress("ADDRESS")
                .recipientPhone("0123123123")
                .userId(null)
                .paymentMethod("INVALID_METHOD") // Invalid payment method
                .recipientEmail("EMAIL")
                .recipientName("NAME")
                .items(List.of(new OrderItemReq(this.testProduct.getId(), "M", - INITIAL_STOCK + 10)))
                .note("NOTE")
                .build();

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(orderReq, new MockHttpServletRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Quantity must be greater than 0");
    }

    @Test
    @Order(10)
    @DisplayName("Should handle empty order items")
    void testCreateOrder_EmptyItems() {
        // Given
        CreateOrderReq orderReq = CreateOrderReq.builder()
                .recipientAddress("ADDRESS")
                .recipientPhone("0123123123")
                .userId(null)
                .paymentMethod("INVALID_METHOD") // Invalid payment method
                .recipientEmail("EMAIL")
                .recipientName("NAME")
                .items(List.of()) // Empty items
                .note("NOTE")
                .build();

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(orderReq, new MockHttpServletRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Order Items cannot be empty");
    }
}