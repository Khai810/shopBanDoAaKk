package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.ChangePasswordReq;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.mappers.AccountMapper;
import com.projectshopbando.shopbandoapi.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;
    private final AccountMapper accountMapper;
//
//    @PostMapping()
//    public ResponseEntity<ResponseObject<?>> createUser(@RequestBody CreateUserReq request) throws BadRequestException {
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ResponseObject.builder()
//                        .data(userMapper.toUserRes(accountService.createUser(request)))
//                        .build());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ResponseObject<?>> getUserById(@PathVariable String id){
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ResponseObject.builder()
//                        .data(userMapper.toUserRes(accountService.getUserById(id)))
//                        .build());
//    }
//
//    @GetMapping()
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ResponseObject<?>> getAllUsers(@RequestParam int page
//            , @RequestParam int size, @RequestParam(required = false) String search){
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ResponseObject.builder()
//                        .data(accountService.getAllUsers(page, size, search).map(userMapper::toUserRes))
//                        .build());
//    }

    @PutMapping("/{customerId}")
    public ResponseEntity<ResponseObject<?>> changePassword(@RequestBody ChangePasswordReq changePasswordReq
            , @PathVariable String customerId){
        return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseObject.builder()
                        .data(accountService.changePassword(changePasswordReq, customerId))
                        .build());
    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ResponseObject<?>> updateAccount(@RequestBody UpdateAccountReq request, @PathVariable String id){
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ResponseObject.builder()
//                        .data(userMapper.toUserRes(accountService.updateUser(request, id)))
//                        .build());
//    }
}
