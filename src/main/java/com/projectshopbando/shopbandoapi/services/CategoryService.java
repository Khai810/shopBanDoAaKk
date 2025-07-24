package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.CategoryCreateReq;
import com.projectshopbando.shopbandoapi.dtos.response.CategoryRes;
import com.projectshopbando.shopbandoapi.entities.Category;
import com.projectshopbando.shopbandoapi.mappers.CategoryMapper;
import com.projectshopbando.shopbandoapi.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public Category getCategoryById(String id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public List<CategoryRes> getAllCategory(){
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toCategoryRes)
                .toList();
    }

    public CategoryRes createCategory(CategoryCreateReq req){
        Category category = categoryMapper.toCategory(req);
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryRes(category);
    }
}
