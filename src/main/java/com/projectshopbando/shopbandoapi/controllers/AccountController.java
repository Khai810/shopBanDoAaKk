package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.ChangePasswordReq;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF') || #customerId == authentication.name")
    @PutMapping("/{customerId}")
    public ResponseEntity<ResponseObject<?>> changePassword(@RequestBody ChangePasswordReq changePasswordReq
            , @PathVariable String customerId){
        return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseObject.builder()
                        .data(accountService.changePassword(changePasswordReq, customerId))
                        .build());
    }
}
