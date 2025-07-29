package com.projectshopbando.shopbandoapi;

import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.request.OrderItemReq;
import com.projectshopbando.shopbandoapi.entities.Category;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.entities.ProductSize;
import com.projectshopbando.shopbandoapi.repositories.CategoryRepository;
import com.projectshopbando.shopbandoapi.repositories.ProductRepository;
import com.projectshopbando.shopbandoapi.repositories.ProductSizeRepository;
import com.projectshopbando.shopbandoapi.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateOrderConcurrencyTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductSizeRepository productSizeRepository;

    private Long productId;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    public void setUpProductWithStock(){
        Category category = Category.builder()
                .name("testCategory")
                .build();
        category = categoryRepository.save(category);

        Product product = Product.builder()
                .name("testProductTrue")
                .price(BigDecimal.valueOf(300000))
                .inStock(true)
                .available(true)
                .category(category)
                .build();
        product = productRepository.save(product);
        productId = product.getId();

        ProductSize productSize = ProductSize.builder()
                .product(product)
                .size("M")
                .quantity(20)
                .build();
        productSizeRepository.save(productSize);
    }
    @Test
    public void testConcurrentOrder_ShouldNotOversell() throws InterruptedException {
        int threadCount = 25; // nhiều hơn số lượng tồn kho
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Future<String>> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            Future<String> future = executor.submit(() -> {
                try {
                    CreateCustomerReq customerReq =
                            new CreateCustomerReq("User " + finalI, "lastName" + finalI
                                    , "u" + finalI + "@mail.com", "0900000" + finalI, "0900000" + finalI);

                    CreateOrderReq orderReq = new CreateOrderReq(
                            List.of(new OrderItemReq(productId, "M", 1)) // quantity 1
                            ,"MOMO", "note"+ finalI
                    );

                    orderService.createOrder(customerReq, orderReq);
                    System.out.println("-----------------------------------");
                    return "✅ SUCCESS " + finalI;
                } catch (Exception e) {
                    return "❌ FAIL " + finalI + ": " + e.getMessage();
                } finally {
                    latch.countDown();
                }
            });
            results.add(future);
        }

        latch.await();
        executor.shutdown();

        results.forEach(future -> {
            try {
                System.out.println(future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Assert quantity cuối cùng
        ProductSize sizeM = productSizeRepository.findByProductIdAndSize(productId, "M")
                .orElseThrow();
        assertEquals(0, sizeM.getQuantity());
    }

}
