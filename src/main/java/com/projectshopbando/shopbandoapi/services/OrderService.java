package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.config.VnPayConfig;
import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreatePaymentUrlReq;
import com.projectshopbando.shopbandoapi.dtos.request.OrderItemReq;
import com.projectshopbando.shopbandoapi.dtos.response.OrderItemsRes;
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

import static java.lang.Long.parseLong;

@Service
@RequiredArgsConstructor
@Validated
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final OrderMapper orderMapper;
    private final StaffService staffService;
    private final EmailSenderService emailSenderService;

    public boolean checkIfOrderExists(String orderId) {
        return orderRepository.existsById(orderId);
    }

    public OrderStatus getOrderStatus(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(orderId));
        return order.getStatus();
    }

    public void updateOrderStatus(String id, OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
        order.setStatus(status);
        orderRepository.save(order);
    }

    public Page<Order> getAllOrder(int page, int size, String search, String status) throws BadRequestException {
        Pageable pageable = PageRequest.of(page, size);
        OrderStatus orderStatus = null;
        if(status != null && !status.isEmpty()) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid order status: " + status);
            }
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
                            , parseLong(row[1].toString()), new BigDecimal(row[2].toString())))
                    .toList();
        }else if(month >= 1 && month <= 12) {
            return orderRepository.getOrderMonthStats(year, month).stream()
                    .map(row -> new OrderStatsDTO( year + "-" + month + "-" + row[0].toString()
                            , parseLong(row[1].toString()), new BigDecimal(row[2].toString())))
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
        validateOrderItems(orderReq.getItems()); // validate order items

        Customer customer = resolveCustomer(orderReq); // resolve customer

        Order order = orderMapper.toOrder(orderReq); // create order entity
        order.setCustomer(customer);

        // Process order items and calc total amount
        OrderItemsRes orderItems = processOrderItems(orderReq.getItems(), order);
        order.setTotalAmount(orderItems.getTotalAmount()); // Total Amount of Order
        order.setOrderedProducts(orderItems.getOrderProducts());
        order.setTotalQuantity(orderItems.getTotalQuantity());

        if(order instanceof OnlineOrder) {
            ((OnlineOrder) order).setEmail(orderReq.getEmail());
            ((OnlineOrder) order).setAddress(orderReq.getAddress());
            ((OnlineOrder) order).setShippingFee(orderReq.getShippingFee() != null ? orderReq.getShippingFee() : BigDecimal.ZERO);
        } else if (order instanceof OfflineOrder) {
            Staff staff = staffService.getStaffById(orderReq.getStaffId());
            ((OfflineOrder) order).setStaff(staff);
        }

        order.setStatus(OrderStatus.UNPAID);
        // Handle paymentMethod
        return processPayment(order, orderReq.getPaymentMethod(), httpRequest);
    }

    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found" + orderId));
        List<OrderProduct> orderProducts = order.getOrderedProducts();

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

    // Helper methods check Customer
    // If CustomerId is provided, get Customer by Id (Priority to the main account)
    // If CustomerId is not provided, get Customer by Phone (For Guest account who have ordered before)
    // If Customer not found by Phone, create new Customer (New Guest account)
    private Customer resolveCustomer(CreateOrderReq orderReq) throws BadRequestException {
        if(orderReq.getCustomerId() != null){
            return customerService.getCustomerById(orderReq.getCustomerId()); // Customer have account
        }
        Customer customer = customerService.getCustomerByPhone(orderReq.getPhone()); // Customer have no account
        if(customer != null){
            return customer;
        }
        else {
            CreateCustomerReq customerReq = CreateCustomerReq.builder()
                    .fullName(orderReq.getName())
                    .phone(orderReq.getPhone())
                    .build();
            return customerService.createCustomer(customerReq); // Create new Customer
        }
    }

    //Helper methods process Order Items
    private OrderItemsRes processOrderItems(List<OrderItemReq> items, Order order) throws BadRequestException {
        List<OrderProduct> orderProducts = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (OrderItemReq item : items) {
            Product product = productService.getProductByIdAndAvailableTrue(item.getProductId());

            productService.decreaseProductSizeQuantity(product.getId(), item.getSize(), item.getQuantity());
            OrderProduct orderProduct = OrderProduct.builder()
                    .product(product)
                    .order(order)
                    .quantity(item.getQuantity())
                    .size(item.getSize())
                    .unitPrice(product.getPrice())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();

            orderProducts.add(orderProduct);
            totalAmount = totalAmount.add(orderProduct.getTotalPrice());
            totalQuantity += orderProduct.getQuantity();
        }
        return new OrderItemsRes(orderProducts, totalAmount, totalQuantity);
    }

    // Helper methods validate Order Items
    private void validateOrderItems(List<OrderItemReq> items) throws BadRequestException {
        if(items == null || items.isEmpty()) {
            throw new BadRequestException("Order Items cannot be empty");
        }
        for (OrderItemReq item : items) {
            if (item.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than 0");
            }
            if (item.getSize() == null || item.getSize().isEmpty()) {
                throw new BadRequestException("Size must be specified for product ID: " + item.getProductId());
            }
        }
    }

    // Helper methods process Payment
    private OrderResponse processPayment(Order order, String paymentMethod, HttpServletRequest httpRequest) throws BadRequestException {
        // Handle COD paymentMethod
        if(PaymentMethod.COD.name().equals(paymentMethod)){
            order.setStatus(OrderStatus.PREPARING);
            order.setPaymentMethod(PaymentMethod.COD);
            order = orderRepository.save(order);
            emailSenderService.sendOrderConfirmationEmail(order);
            return OrderResponse.builder()
                    .order(orderMapper.toOrderDto(order))
                    .payment(null)
                    .build();
        }else if(PaymentMethod.VNPAY.name().equals(paymentMethod)) {
            // Handle VNPAY paymentMethod
            order.setPaymentMethod(PaymentMethod.VNPAY);
            order = orderRepository.save(order);

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
        } else if(PaymentMethod.CASH.name().equals(paymentMethod) ||
                PaymentMethod.BANK_TRANSFER.name().equals(paymentMethod)) {
            order.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
            order.setStatus(OrderStatus.COMPLETED);
            order = orderRepository.save(order);
            return OrderResponse.builder()
                    .order(orderMapper.toOrderDto(order))
                    .payment(null)
                    .build();
        }
        else throw new BadRequestException("Invalid payment method");
    }
}