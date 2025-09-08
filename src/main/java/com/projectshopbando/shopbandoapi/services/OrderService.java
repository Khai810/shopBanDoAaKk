package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.config.VnPayConfig;
import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreatePaymentUrlReq;
import com.projectshopbando.shopbandoapi.dtos.request.OrderItemReq;
import com.projectshopbando.shopbandoapi.dtos.response.OrderItemsRes;
import com.projectshopbando.shopbandoapi.dtos.response.OrderResponse;
import com.projectshopbando.shopbandoapi.dtos.response.OrderStatsDTO;
import com.projectshopbando.shopbandoapi.entities.Customer;
import com.projectshopbando.shopbandoapi.entities.Order;
import com.projectshopbando.shopbandoapi.entities.OrderProduct;
import com.projectshopbando.shopbandoapi.entities.Product;
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
            order.setTotalAmount(orderItems.getTotalAmount());
            order.setOrderedProduct(orderItems.getOrderProducts());
            order.setStatus(OrderStatus.UNPAID);
            // Handle paymentMethod
            return processPayment(order, orderReq.getPaymentMethod(), httpRequest);
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

    private Customer resolveCustomer(CreateOrderReq orderReq) {
        if(orderReq.getUserId() != null){
            return userService.getUserById(orderReq.getUserId());
        }
        else {
            CreateCustomerReq customerReq = CreateCustomerReq.builder()
                    .fullName(orderReq.getRecipientName())
                    .email(orderReq.getRecipientEmail())
                    .phone(orderReq.getRecipientPhone())
                    .address(orderReq.getRecipientAddress())
                    .build();
            return customerService.createCustomer(customerReq);
        }
    }

    private OrderItemsRes processOrderItems(List<OrderItemReq> items, Order order) throws BadRequestException {
        List<OrderProduct> orderProducts = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

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
        }
        return new OrderItemsRes(orderProducts, totalAmount);
    }

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

    private OrderResponse processPayment(Order order, String paymentMethod, HttpServletRequest httpRequest) throws BadRequestException {
        // Handle COD paymentMethod
        if(PaymentMethod.COD.name().equals(paymentMethod)){
            order.setStatus(OrderStatus.PREPARING);
            order.setPaymentMethod(PaymentMethod.COD);
            order = orderRepository.save(order);

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
        }
        else throw new BadRequestException("Invalid payment method");
    }
}