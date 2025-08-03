package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.config.VnPayConfig;
import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderPayload;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.mappers.OrderMapper;
import com.projectshopbando.shopbandoapi.services.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping()
    public ResponseEntity<ResponseObject<?>> getAllOrder(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(orderMapper.toOrderResPayloadList(
                                orderService.getAllOrder()))
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> getOrderById(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(orderMapper.toOrderResPayload(
                                orderService.getOrderById(id)))
                        .build());
    }

    @PostMapping()
    public ResponseEntity<ResponseObject<?>> createOrder(@RequestBody @Valid CreateOrderPayload payload, HttpServletRequest request) throws BadRequestException {
        String ipAdress = VnPayConfig.getIpAddress(request);
        CreateCustomerReq customerReq = payload.getCustomerReq();
        customerReq.setIpAddress(ipAdress);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseObject.builder()
                        .data(orderService.createOrder(customerReq
                                , payload.getOrderReq()))
                        .build()
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> deleteOrder(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                    ResponseObject.builder()
                    .message(orderService.deleteOrderById(id))
                    .build()
        );
    }

}
