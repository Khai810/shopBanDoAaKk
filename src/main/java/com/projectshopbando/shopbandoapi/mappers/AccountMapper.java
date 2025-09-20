package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateAccountReq;
import com.projectshopbando.shopbandoapi.entities.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toAccount(CreateAccountReq request);
}
