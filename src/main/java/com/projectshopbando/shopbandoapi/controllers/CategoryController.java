package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.CategoryCreateReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateCategoryReq;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.mappers.CategoryMapper;
import com.projectshopbando.shopbandoapi.services.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@AllArgsConstructor
public class CategoryController {
    private final CategoryMapper categoryMapper;
    private final CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<ResponseObject<?>> getAllCategory() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(categoryMapper.toCategoryResList(
                                categoryService.getAllCategory()))
                        .build());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    public ResponseEntity<ResponseObject<?>> getAllCategoryAdmin(@RequestParam int page,@RequestParam int size,
                                                                 @RequestParam(required = false) String search) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(categoryService.getAllCategoryAdmin(page, size, search)
                                .map(categoryMapper::toCategoryRes))
                        .build());
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    public ResponseEntity<ResponseObject<?>> getCategoryByIdAdmin(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(categoryMapper.toCategoryRes(categoryService.getCategoryById(id)))
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(categoryMapper.toCategoryRes(
                                categoryService.getCategoryByIdAndIsDisabledFalse(id)))
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    @PostMapping("/admin")
    public ResponseEntity<ResponseObject<?>> createCategory(@RequestBody CategoryCreateReq req) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(categoryMapper.toCategoryRes(
                                categoryService.createCategory(req)))
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    @PutMapping("/admin/{id}")
    public  ResponseEntity<ResponseObject<?>> updateCategory(@PathVariable Long id, @RequestBody UpdateCategoryReq req) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(categoryMapper.toCategoryRes(
                                categoryService.updateCategory(id, req)))
                        .build());
    }
}
