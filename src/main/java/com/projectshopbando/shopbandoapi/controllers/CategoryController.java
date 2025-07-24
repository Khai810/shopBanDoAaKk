package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.request.CategoryCreateReq;
import com.projectshopbando.shopbandoapi.dtos.response.CategoryRes;
import com.projectshopbando.shopbandoapi.services.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
public class CategoryController {
    private CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<List<CategoryRes>> getAllCategory() {
        return ResponseEntity.ok(categoryService.getAllCategory());
    }

    @PostMapping()
    public ResponseEntity<CategoryRes> createCategory(@RequestBody CategoryCreateReq req) {
        return ResponseEntity.ok(categoryService.createCategory(req));
    }

}
