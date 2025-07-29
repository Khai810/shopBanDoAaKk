package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateCustomerReq;
import com.projectshopbando.shopbandoapi.dtos.response.CustomerRes;
import com.projectshopbando.shopbandoapi.entities.Customer;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toCustomer(CreateCustomerReq request);

    CustomerRes toCustomerRes(Customer customer);

    List<CustomerRes> toCustomerRes(List<Customer> customers);
}
