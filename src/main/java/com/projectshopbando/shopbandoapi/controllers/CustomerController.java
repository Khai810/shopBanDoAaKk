package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.mappers.CustomerMapper;
import com.projectshopbando.shopbandoapi.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @GetMapping()
    public ResponseEntity<ResponseObject<?>> getAllCustomers() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(customerMapper
                                .toCustomerRes(customerService.getAllCustomers()))
                        .build());
    }
}
