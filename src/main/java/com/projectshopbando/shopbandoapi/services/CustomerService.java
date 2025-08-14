package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.entities.Customer;
import com.projectshopbando.shopbandoapi.mappers.CustomerMapper;
import com.projectshopbando.shopbandoapi.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public Page<Customer> getAllCustomers(int page, int size, String phone) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findByPhoneContainingOrderByCreatedAtDesc(phone, pageable);
    }
    @Transactional
    public Customer createCustomer(@Valid CreateCustomerReq request) {
        Customer customer = customerMapper.toCustomer(request);
        return customerRepository.save(customer);
    }
}
