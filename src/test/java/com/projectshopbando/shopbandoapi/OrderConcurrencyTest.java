package com.projectshopbando.shopbandoapi;

import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.request.OrderItemReq;
import com.projectshopbando.shopbandoapi.dtos.response.OrderResponse;
import com.projectshopbando.shopbandoapi.entities.Category;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.entities.ProductSize;
import com.projectshopbando.shopbandoapi.repositories.CategoryRepository;
import com.projectshopbando.shopbandoapi.repositories.ProductRepository;
import com.projectshopbando.shopbandoapi.repositories.ProductSizeRepository;
import com.projectshopbando.shopbandoapi.services.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderConcurrencyTest {

    @Autowired private OrderService orderService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSizeRepository productSizeRepository;
    @Autowired private CategoryRepository categoryRepository;

    private static Long PRODUCT_ID;
    private static int INITIAL_STOCK = 3; // Số lượng sản phẩm ban đầu trong kho
    private static int ORDER_QUANTITY = 1; // Số lượng mỗi đơn hàng đặt
    @BeforeEach
    @Transactional
    @Commit
    void setup() {
        Category category = Category.builder()
                .name("Limited Category")
                .description("Category for concurrency test")
                .imageUrl("testurl")
                .isDisabled(false)
                .build();
        category = categoryRepository.save(category);
        Product product = Product.builder()
                .name("Limited Product")
                .price(BigDecimal.valueOf(100000))
                .discountPercent(BigDecimal.ZERO)
                .available(true)
                .category(category)
                .build();
        product = productRepository.save(product);

        ProductSize productSize = ProductSize.builder()
                .product(product)
                .size("M")
                .quantity(INITIAL_STOCK) // Chỉ có 2 sản phẩm trong kho
                .build();
        productSize = productSizeRepository.save(productSize);
        product.setSizes(List.of(productSize));
        product = productRepository.save(product);
        this.PRODUCT_ID = product.getId();
    }

    @Test
    public void testOrderConcurrency() throws InterruptedException {
        // Given
        int numberOfThreads = 10; // Reduced to avoid overwhelming the system
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

                    CreateOrderReq orderReq = new CreateOrderReq().builder()
                            .name("Customer" + threadIndex)
                            .phone("012312312" + threadIndex)
                            .email(threadIndex + "@mail.com")
                            .address("Address " + threadIndex)
                            .items(List.of(new OrderItemReq(PRODUCT_ID, "M", ORDER_QUANTITY)))
                            .totalAmount(BigDecimal.valueOf(100000 * ORDER_QUANTITY))
                            .discount(BigDecimal.ZERO)
                            .tax(BigDecimal.ZERO)
                            .shippingFee(BigDecimal.ZERO)
                            .paymentMethod("COD")
                            .note("Note " + threadIndex)
                            .type("ONLINE")
                            .build();

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
                .findByProductIdAndSize(PRODUCT_ID, "M")
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
}
