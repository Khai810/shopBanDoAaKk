package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.ChangePasswordReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateUserReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateUserReq;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.mappers.UserMapper;
import com.projectshopbando.shopbandoapi.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping()
    public ResponseEntity<ResponseObject<?>> createUser(@RequestBody CreateUserReq request){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(userMapper.toUserRes(userService.createUser(request)))
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> getUserById(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(userMapper.toUserRes(userService.getUserById(id)))
                        .build());
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject<?>> getAllUsers(@RequestParam int page
            , @RequestParam int size, @RequestParam(required = false) String search){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(userService.getAllUsers(page, size, search).map(userMapper::toUserRes))
                        .build());
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<ResponseObject<?>> changePassword(@RequestBody ChangePasswordReq changePasswordReq
            , @PathVariable String id){
        return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseObject.builder()
                        .data(userService.changePassword(changePasswordReq, id))
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> updateUser(@RequestBody UpdateUserReq request, @PathVariable String id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(userMapper.toUserRes(userService.updateUser(request, id)))
                        .build());
    }
}
