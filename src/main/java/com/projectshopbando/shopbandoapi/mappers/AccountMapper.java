package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateAccountReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateAccountReq;
import com.projectshopbando.shopbandoapi.entities.Account;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toAccount(CreateAccountReq request);

    void toUpdateAccount(@MappingTarget Account account, UpdateAccountReq request);
}
