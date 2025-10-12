package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.ProductCreateRequest;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateProductReq;
import com.projectshopbando.shopbandoapi.dtos.response.ProductAdminResponse;
import com.projectshopbando.shopbandoapi.dtos.response.ProductResponse;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.mappers.ProductMapper;
import com.projectshopbando.shopbandoapi.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<ResponseObject<?>> getAllProduct(@RequestParam int page, @RequestParam int size
            , @RequestParam(required = false) String name, @RequestParam(required = false) Long categoryId
            , @RequestParam(required = false) Boolean available) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .status("success")
                        .data(productService.getAllProduct(page, size, name, categoryId, available).map(productMapper::toProductCardRes))
                        .build()
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<ResponseObject<?>> getProductById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .status("success")
                        .data(productMapper.toProductAdminResponse(productService.getProductById(id)))
                        .build()
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<ProductResponse>> getProductByIdAndAvailableTrue(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.<ProductResponse>builder()
                        .data(productMapper.toProductResponse(
                                productService.getProductByIdAndAvailableTrue(id)))
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<ResponseObject<ProductResponse>> createProduct(@RequestBody ProductCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseObject.<ProductResponse>builder()
                        .data(productMapper.toProductResponse(
                                productService.createProduct(request)))
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{id}/disable")
    public ResponseEntity<ResponseObject<?>> disableProduct(@PathVariable Long id){
        productService.disableProduct(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ResponseObject.builder()
                        .data("disabled")
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}")
    public ResponseEntity<ResponseObject<?>> updateProduct(@PathVariable Long id, @RequestBody UpdateProductReq request){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.<ProductAdminResponse>builder()
                        .data(productMapper.toProductAdminResponse(
                                productService.updateProduct(id, request)))
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    @GetMapping("/admin/stats")
    public ResponseEntity<ResponseObject<?>> getProductStats(@RequestParam int year, @RequestParam(required = false) Integer month) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(productService.getProductStats(year, month))
                        .build());
    }
}
