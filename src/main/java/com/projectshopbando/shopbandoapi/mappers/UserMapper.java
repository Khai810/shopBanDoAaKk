package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateUserReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateUserReq;
import com.projectshopbando.shopbandoapi.dtos.response.UserRes;
import com.projectshopbando.shopbandoapi.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(CreateUserReq request);

    @Mapping(target = "fullName", source = "request.fullName")
    void toUpdateUser(@MappingTarget User user, UpdateUserReq request);

    UserRes toUserRes(User user);
}
