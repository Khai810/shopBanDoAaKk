package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.response.IpnRes;
import com.projectshopbando.shopbandoapi.services.VNPayIpnHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPayIpnHandler vnPayIpnHandler;

    @GetMapping("/vnpay-ipn")
    public IpnRes processIpn(@RequestParam Map<String, String> params){
        return vnPayIpnHandler.ipnHandler(params);
    }
}
