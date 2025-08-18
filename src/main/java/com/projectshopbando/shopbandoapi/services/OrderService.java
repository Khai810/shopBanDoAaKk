package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.config.VnPayConfig;
import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreatePaymentUrlReq;
import com.projectshopbando.shopbandoapi.dtos.request.OrderItemReq;
import com.projectshopbando.shopbandoapi.dtos.response.OrderResponse;
import com.projectshopbando.shopbandoapi.dtos.response.OrderStatsDTO;
import com.projectshopbando.shopbandoapi.entities.*;
import com.projectshopbando.shopbandoapi.enums.OrderStatus;
import com.projectshopbando.shopbandoapi.enums.PaymentMethod;
import com.projectshopbando.shopbandoapi.exception.NotFoundException;
import com.projectshopbando.shopbandoapi.mappers.OrderMapper;
import com.projectshopbando.shopbandoapi.repositories.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final UserService userService;

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

    public Page<Order> getAllOrder(int page, int size, String search, String status) {
        Pageable pageable = PageRequest.of(page, size);
        OrderStatus orderStatus = null;
        if(status != null && !status.isEmpty()) {
            orderStatus = OrderStatus.valueOf(status);
        }
        return orderRepository.adminFindAll(search, orderStatus , pageable);
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + id));
    }

    public List<OrderStatsDTO> getOrderStats(int year, int month) {
        if(month == 0 ) {
            return orderRepository.getOrderYearStats(year).stream()
                    .map(row -> new OrderStatsDTO(year + "-" + row[0].toString()
                            , (Long) row[1], (BigDecimal) row[2]))
                    .toList();
        }else if(month >= 1 && month <= 12) {
            return orderRepository.getOrderMonthStats(year, month).stream()
                    .map(row -> new OrderStatsDTO(year + "-" + month + "-" + row[0].toString()
                            , (Long) row[1], (BigDecimal) row[2]))
                    .toList();
        }
        try {
            throw new BadRequestException();
        } catch (BadRequestException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Order> getOrderByCustomerId(String customerId) {
        return orderRepository.findAllByCustomerIdOrderByOrderDateDesc(customerId);
    }

    @Transactional
    public OrderResponse createOrder(@Valid CreateOrderReq orderReq, HttpServletRequest httpRequest) throws BadRequestException {

        Order order = orderMapper.toOrder(orderReq);

        if(orderReq.getUserId() != null){
            User customer = userService.getUserById(orderReq.getUserId());
            order.setCustomer(customer);
        }
        else {
            CreateCustomerReq customerReq = CreateCustomerReq.builder()
                    .fullName(orderReq.getRecipientName())
                    .email(orderReq.getRecipientEmail())
                    .phone(orderReq.getRecipientPhone())
                    .address(orderReq.getRecipientAddress())
                    .build();
            Customer customer = customerService.createCustomer(customerReq);
            order.setCustomer(customer);
        }

        order.setStatus(OrderStatus.UNPAID);
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderProduct> orderProducts = new ArrayList<>();
        List<OrderItemReq> items = orderReq.getItems();
        // Set OrderProduct
        for (OrderItemReq item : items) {
            Product product = productService.getProductByIdAndAvailableTrue(item.getProductId());
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

        if(orderReq.getPaymentMethod().equals(String.valueOf(PaymentMethod.COD))){
            order.setPaymentMethod(PaymentMethod.COD);
            order.setStatus(OrderStatus.PREPARING);
            order = orderRepository.save(order);
            return OrderResponse.builder()
                    .order(orderMapper.toOrderDto(order))
                    .payment(null)
                    .build();
        }

         var customerIpAddress = VnPayConfig.getIpAddress(httpRequest);

        CreatePaymentUrlReq paymentRequest = CreatePaymentUrlReq.builder()
                .amount(order.getTotalAmount())
                .ipAddress(customerIpAddress)
                .txnRef(order.getId())
                .build();

        return OrderResponse.builder()
                .order(orderMapper.toOrderDto(order))
                .payment(paymentService.initPaymentUrl(paymentRequest))
                .build();
    }

    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found" + orderId));
        List<OrderProduct> orderProducts = order.getOrderedProduct();

        for(OrderProduct orderProduct : orderProducts) {
            int quantity = orderProduct.getQuantity();
            String size = orderProduct.getSize();
            Long productId = orderProduct.getProduct().getId();
            productService.increaseProductSizeQuantity(productId, size, quantity);
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public String deleteOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        orderRepository.delete(order);
        return "Order deleted";
    }
}