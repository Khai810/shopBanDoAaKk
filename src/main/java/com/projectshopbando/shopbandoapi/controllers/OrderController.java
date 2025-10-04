package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.enums.OrderStatus;
import com.projectshopbando.shopbandoapi.mappers.OrderMapper;
import com.projectshopbando.shopbandoapi.services.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping("/{id}/status")
    public ResponseEntity<ResponseObject<?>> getOrderStatus(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(orderService.getOrderStatus(id))
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<ResponseObject<?>> getAllOrder(
            @RequestParam int page
            , @RequestParam int size
            , @RequestParam(required = false) String search
            , @RequestParam(required = false) String status) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(orderService.getAllOrder(page, size, search, status).map(orderMapper::toOrderDto))
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/stats")
    public ResponseEntity<ResponseObject<?>> getOrderStats(@RequestParam int year, @RequestParam @Min(value = 0) int month) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(orderService.getOrderStats(year, month))
                        .build());
    }

//    @PostAuthorize("returnObject.body.data.customer.id == authentication.name")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> getOrderById(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(orderMapper.toOrderDto(
                                orderService.getOrderById(id)))
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF') || authentication.name == #customerId")
    @GetMapping("/customers")
    public ResponseEntity<ResponseObject<?>> getOrderByCustomerId(@RequestParam String customerId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(orderService.getOrderByCustomerId(customerId).stream().map(orderMapper::toOrderDto).toList())
                        .build());
    }

    @PostMapping()
    public ResponseEntity<ResponseObject<?>> createOrder(@RequestBody @Valid CreateOrderReq orderRequest
            , HttpServletRequest httpRequest) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseObject.builder()
                        .data(orderService.createOrder(orderRequest
                                , httpRequest))
                        .build()
                );
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> deleteOrder(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                    ResponseObject.builder()
                    .message(orderService.deleteOrderById(id))
                    .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    @PutMapping("/admin/{id}")
    public  ResponseEntity<?> updateOrderStatus(@PathVariable String id, @RequestParam String status){
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        orderService.updateOrderStatus(id, orderStatus);
        return ResponseEntity.ok().build();
    }
}
