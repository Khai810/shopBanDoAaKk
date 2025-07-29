package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.entities.Customer;
import com.projectshopbando.shopbandoapi.mappers.CustomerMapper;
import com.projectshopbando.shopbandoapi.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    @Transactional
    public Customer createCustomer(@Valid CreateCustomerReq request) {
        Customer customer = customerMapper.toCustomer(request);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer findOrCreateCustomerByPhone(@Valid CreateCustomerReq req) {
        return customerRepository.findByPhone(req.getPhone())
                .orElseGet(() -> createCustomer(req));
    }
}
