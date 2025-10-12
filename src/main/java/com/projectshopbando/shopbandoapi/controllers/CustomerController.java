package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.mappers.CustomerMapper;
import com.projectshopbando.shopbandoapi.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    @GetMapping()
    public ResponseEntity<ResponseObject<?>> getAllCustomers(@RequestParam int page
            , @RequestParam int size, @RequestParam String phone) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(customerService.getAllCustomers(page, size, phone).map(customerMapper::toCustomerRes))
                        .build());
    }

    @PostAuthorize("returnObject.body.data.id == authentication.name || hasRole('ADMIN') || hasRole('STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> getCustomerById(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(customerMapper.toCustomerRes(customerService.getCustomerById(id)))
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    @GetMapping("/admin/with-account")
    public ResponseEntity<ResponseObject<?>> getAllCustomerHavingAccount(@RequestParam int page
            , @RequestParam int size, @RequestParam String phone) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(customerService.getAllCustomerHavingAccount(page, size, phone).map(customerMapper::toCustomerRes))
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')|| hasRole('STAFF') || #id == authentication.name")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> updateCustomer(@PathVariable String id, @RequestBody UpdateCustomerReq req) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(customerMapper.toCustomerRes(customerService.updateCustomer(id, req)))
                        .build());
    }

    @PostMapping()
    public ResponseEntity<ResponseObject<?>> createCustomer(@RequestBody CreateCustomerReq req) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(customerMapper.toCustomerRes(customerService.createCustomer(req)))
                        .build());
    }
}
