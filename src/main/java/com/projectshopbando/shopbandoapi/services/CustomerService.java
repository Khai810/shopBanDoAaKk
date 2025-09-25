package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.CreateAccountReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateCustomerReq;
import com.projectshopbando.shopbandoapi.entities.Account;
import com.projectshopbando.shopbandoapi.entities.Customer;
import com.projectshopbando.shopbandoapi.mappers.CustomerMapper;
import com.projectshopbando.shopbandoapi.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class CustomerService {
    private final AccountService accountService;
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public Page<Customer> getAllCustomers(int page, int size, String phone) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findByPhoneContainingOrderByCreatedAtDesc(phone, pageable);
    }

    // Create customer. If customer with the same phone number exists, update the customer instead.
    @Transactional
    public Customer createCustomer(@Valid CreateCustomerReq request) throws BadRequestException {
        Customer customer = getCustomerByPhone(request.getPhone());
        if(customer != null && customer.getAccount() != null) {     // If customer already has an account
            throw new BadRequestException("Customer already exists");
        }

        if(customer != null) {                                // If customer exists but doesn't have an account, update the customer
            customer.setFullName(request.getFullName());
            if (request.getEmail() != null && request.getPassword() != null) {
                customer.setAccount(buildAccount(request, customer));
            }
            return customerRepository.save(customer);
        }

        // New Customer creation
        customer = customerMapper.toCustomer(request);              // Create new customer
        if(request.getEmail() != null && request.getPassword() != null) { // If email and password are provided, create an account
            customer.setAccount(buildAccount(request, customer));
        }
        return customerRepository.save(customer);
    }

    public Customer getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone).orElse(null);
    }

    public Page<Customer> getAllCustomerHavingAccount(int page, int size, String phone) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findByPhoneContainingHavingAccount(phone, pageable);
    }

    public Customer getCustomerById(String id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer updateCustomer(String id, @Valid UpdateCustomerReq req) {
        Customer customer = getCustomerById(id);
        customerMapper.toUpdateCustomer(customer, req);
        return customerRepository.save(customer);
    }

    private Account buildAccount(CreateCustomerReq request, Customer customer) {
        CreateAccountReq createAccountReq = CreateAccountReq.builder()
                .dob(request.getDob())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        return accountService.createAccount(createAccountReq, customer, null);
    }
}
