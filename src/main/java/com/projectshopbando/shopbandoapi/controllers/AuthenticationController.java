package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.AuthenticateReq;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ResponseObject<?>> authenticate(@RequestBody AuthenticateReq request){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(authenticationService.authenticate(request))
                        .build());
    }
}
