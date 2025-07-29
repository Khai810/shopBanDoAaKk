package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.CategoryCreateReq;
import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.mappers.CategoryMapper;
import com.projectshopbando.shopbandoapi.services.CategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@AllArgsConstructor
public class CategoryController {
    private final CategoryMapper categoryMapper;
    private CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<ResponseObject<?>> getAllCategory() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(categoryMapper.toCategoryResList(
                                categoryService.getAllCategory()))
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(categoryMapper.toCategoryRes(
                                categoryService.getCategoryById(id)))
                        .build());
    }

    @PostMapping()
    public ResponseEntity<ResponseObject<?>> createCategory(@RequestBody CategoryCreateReq req) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(categoryMapper.toCategoryRes(
                                categoryService.createCategory(req)))
                        .build());
    }

}
