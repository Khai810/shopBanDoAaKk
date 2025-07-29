package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.ProductCreateRequest;
import com.projectshopbando.shopbandoapi.dtos.response.ProductResponse;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.mappers.ProductMapper;
import com.projectshopbando.shopbandoapi.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping("/admin")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> getAllProduct(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.<List<ProductResponse>>builder()
                        .status("success")
                        .data(productService.getAllProduct().stream()
                                .map(productMapper::toProductResponse)
                                .toList())
                        .build()
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<ProductResponse>> getProductById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.<ProductResponse>builder()
                        .data(productMapper.toProductResponse(
                                productService.getProductById(id)))
                        .build()
                );
    }

    @GetMapping("/landing")
    public ResponseEntity<ResponseObject<?>> getLandingProduct(){
        List<Product> products = productService.getLandingProduct();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(productMapper.toProductCardResList(products))
                        .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseObject<?>> searchProducts(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "9") int size,
                                                            @RequestParam(required = false) Long categoryId,
                                                            @RequestParam(required = false) Long excludeId,
                                                            @RequestParam(required = false) String name,
                                                            @RequestParam(required = false) BigDecimal minPrice,
                                                            @RequestParam(required = false) BigDecimal maxPrice){
        Page<Product> products = productService.searchProduct(page, size, categoryId, excludeId, name, minPrice, maxPrice);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(products.map(productMapper::toProductCardRes))
                        .build());
    }

    @PostMapping()
    public ResponseEntity<ResponseObject<ProductResponse>> createProduct(@RequestBody ProductCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseObject.<ProductResponse>builder()
                        .data(productMapper.toProductResponse(
                                productService.createProduct(request)))
                        .build());
    }


}
