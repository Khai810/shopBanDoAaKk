package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderPayload;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.mappers.OrderMapper;
import com.projectshopbando.shopbandoapi.services.OrderService;
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
                        .data(orderMapper.toOrderResponse(
                                orderService.getOrderById(id)))
                        .build());
    }

    @PostMapping()
    public ResponseEntity<ResponseObject<?>> createOrder(@RequestBody @Valid CreateOrderPayload payload) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseObject.builder()
                        .data(orderMapper.toOrderResPayload(
                                orderService.createOrder(payload.getCustomerReq()
                                        , payload.getOrderReq())))
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
