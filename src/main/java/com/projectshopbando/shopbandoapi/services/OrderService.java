package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.request.OrderItemReq;
import com.projectshopbando.shopbandoapi.entities.Customer;
import com.projectshopbando.shopbandoapi.entities.Order;
import com.projectshopbando.shopbandoapi.entities.OrderProduct;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.enums.PaymentMethod;
import com.projectshopbando.shopbandoapi.exception.NotFoundException;
import com.projectshopbando.shopbandoapi.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;

    public List<Order> getAllOrder() {
        return orderRepository.findAll()
                .stream().toList();
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + id));
    }

    @Transactional
    public Order createOrder(@Valid CreateCustomerReq customerReq, @Valid CreateOrderReq orderReq) throws BadRequestException {

        Customer customer = customerService.findOrCreateCustomerByPhone(customerReq);
        Order order = new Order();
        order.setCustomer(customer);
        order.setNote(orderReq.getNote());
        order.setPaymentMethod(PaymentMethod.valueOf(orderReq.getPaymentMethod()));

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderProduct> orderProducts = new ArrayList<>();
        for (OrderItemReq item : orderReq.getItems()) {
            Product product = productService.getProductById(item.getProductId());
            productService.decreaseProductSizeQuantity(product.getId(), item.getSize(), item.getQuantity());
            OrderProduct orderProduct = OrderProduct.builder()
                    .size(item.getSize())
                    .product(product)
                    .order(order)
                    .quantity(item.getQuantity())
                    .unitPrice(product.getPrice())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
            orderProducts.add(orderProduct);
            totalAmount = totalAmount.add(orderProduct.getTotalPrice());
        }
        order.setTotalAmount(totalAmount);
        order.setOrderedProduct(orderProducts);

        return orderRepository.save(order);
    }

    public String deleteOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        orderRepository.delete(order);
        return "Order deleted";
    }
}