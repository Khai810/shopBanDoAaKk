package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateStaffReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateStaffReq;
import com.projectshopbando.shopbandoapi.dtos.response.StaffDTO;
import com.projectshopbando.shopbandoapi.entities.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StaffMapper {
    Staff toStaff(CreateStaffReq req);

    @Mapping(target = "email", source = "account.email")
    @Mapping(target = "dob", source = "account.dob")
    @Mapping(target = "role", source = "account.role")
    StaffDTO toStaffDTO(Staff staff);

    void updateStaff(@MappingTarget Staff staff, UpdateStaffReq req);
}
