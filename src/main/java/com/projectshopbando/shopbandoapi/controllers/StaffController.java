package com.projectshopbando.shopbandoapi.controllers;


import com.projectshopbando.shopbandoapi.dtos.request.CreateStaffReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateStaffReq;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.mappers.StaffMapper;
import com.projectshopbando.shopbandoapi.services.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staffs")
@RequiredArgsConstructor
public class StaffController {
    private final StaffService staffService;
    private final StaffMapper staffMapper;

    @PostAuthorize("returnObject.body.data.id == authentication.name or hasRole('ADMIN') or hasRole('STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> getStaffById(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .status("success")
                        .data(staffMapper.toStaffDTO(staffService.getStaffById(id)))
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SneakyThrows
    @PostMapping
    public ResponseEntity<ResponseObject<?>> createStaff(@RequestBody CreateStaffReq req){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseObject.builder()
                        .status("success")
                        .data(staffMapper.toStaffDTO(staffService.createStaff(req)))
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ResponseObject<?>> getAllStaffs(@RequestParam int page,@RequestParam int size, @RequestParam String search){
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .status("success")
                        .data(staffService.getAllStaffs(page, size, search).map(staffMapper::toStaffDTO))
                        .build()
        );

    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF') || #id == authentication.name")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> updateStaff(@PathVariable String id, @RequestBody UpdateStaffReq req){
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .status("success")
                        .data(staffMapper.toStaffDTO(staffService.updateStaff(id, req)))
                        .build()
        );
    }

}
