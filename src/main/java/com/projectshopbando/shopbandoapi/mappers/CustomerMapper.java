package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.response.CustomerRes;
import com.projectshopbando.shopbandoapi.entities.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toCustomer(CreateCustomerReq request);

    @Mapping(source = "dob", target = "account.dob")
    @Mapping(source = "email", target = "account.email")
    void toUpdateCustomer(@MappingTarget Customer customer, UpdateCustomerReq request);

    @Mapping(source = "customer.account.email", target = "email")
    @Mapping(source = "customer.account.dob", target = "dob")
    @Mapping(source = "customer.account.role", target = "role")
    CustomerRes toCustomerRes(Customer customer);

    List<CustomerRes> toCustomerRes(List<Customer> customers);
}
