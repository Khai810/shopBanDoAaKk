package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreatePaymentUrlReq;
import com.projectshopbando.shopbandoapi.dtos.request.OrderItemReq;
import com.projectshopbando.shopbandoapi.dtos.response.OrderResponse;
import com.projectshopbando.shopbandoapi.entities.Customer;
import com.projectshopbando.shopbandoapi.entities.Order;
import com.projectshopbando.shopbandoapi.entities.OrderProduct;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.enums.OrderStatus;
import com.projectshopbando.shopbandoapi.enums.PaymentMethod;
import com.projectshopbando.shopbandoapi.exception.NotFoundException;
import com.projectshopbando.shopbandoapi.mappers.OrderMapper;
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
    private final PaymentService paymentService;
    private final OrderMapper orderMapper;

    public boolean checkIfOrderExists(String orderId) {
        return orderRepository.existsById(orderId);
    }

    public OrderStatus getOrderStatus(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(orderId));
        return order.getStatus();
    }

    public void updateOrderStatus(Order order, OrderStatus status) {
        order.setStatus(status);
        orderRepository.save(order);
    }

    public List<Order> getAllOrder() {
        return orderRepository.findAll()
                .stream().toList();
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + id));
    }

    @Transactional
    public OrderResponse createOrder(@Valid CreateCustomerReq customerReq, @Valid CreateOrderReq orderReq) throws BadRequestException {

        Customer customer = customerService.createCustomer(customerReq);
        Order order = new Order();
        order.setCustomer(customer);
        order.setNote(orderReq.getNote());
        order.setPaymentMethod(PaymentMethod.valueOf(orderReq.getPaymentMethod()));
        order.setStatus(OrderStatus.UNPAID);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderProduct> orderProducts = new ArrayList<>();
        List<OrderItemReq> items = orderReq.getItems();
        // Set OrderProduct
        for (OrderItemReq item : items) {
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

        order = orderRepository.save(order);

        CreatePaymentUrlReq paymentRequest = CreatePaymentUrlReq.builder()
                .amount(order.getTotalAmount())
                .ipAddress(customer.getAddress())
                .txnRef(order.getId())
                .build();

        return OrderResponse.builder()
                .order(orderMapper.toOrderDto(order))
                .payment(paymentService.initPaymentUrl(paymentRequest))
                .build();
    }

    @Transactional
    public void cancelOrder(String orderId) throws BadRequestException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found" + orderId));
        List<OrderProduct> orderProducts = order.getOrderedProduct();

        for(OrderProduct orderProduct : orderProducts) {
            int quantity = orderProduct.getQuantity();
            String size = orderProduct.getSize();
            Long productId = orderProduct.getProduct().getId();
            productService.increaseProductSizeQuantity(productId, size, quantity);
        }

        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);
    }

    public String deleteOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        orderRepository.delete(order);
        return "Order deleted";
    }
}