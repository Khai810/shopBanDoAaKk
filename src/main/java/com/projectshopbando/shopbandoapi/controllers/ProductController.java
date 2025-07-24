package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.ProductCreateRequest;
import com.projectshopbando.shopbandoapi.dtos.response.ProductResponse;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {
    private ProductService productService;

    @GetMapping()
    public ResponseEntity<ResponseObject<List<ProductResponse>>> getAllProduct(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.<List<ProductResponse>>builder()
                        .status("success")
                        .data(productService.getAllProduct())
                        .build()
                );
    }

    @PostMapping()
    public ResponseEntity<ResponseObject<ProductResponse>> createProduct(@RequestBody ProductCreateRequest request){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.<ProductResponse>builder()
                        .data(productService.createProduct(request))
                        .build());
    }
}
