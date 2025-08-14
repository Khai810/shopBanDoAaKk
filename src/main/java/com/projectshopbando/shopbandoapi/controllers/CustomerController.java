package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.mappers.CustomerMapper;
import com.projectshopbando.shopbandoapi.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<ResponseObject<?>> getAllCustomers(@RequestParam int page
            , @RequestParam int size, @RequestParam String phone) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(customerService.getAllCustomers(page, size, phone).map(customerMapper::toCustomerRes))
                        .build());
    }
}
