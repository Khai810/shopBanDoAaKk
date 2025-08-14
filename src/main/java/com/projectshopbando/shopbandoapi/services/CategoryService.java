package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.CategoryCreateReq;
import com.projectshopbando.shopbandoapi.entities.Category;
import com.projectshopbando.shopbandoapi.exception.NotFoundException;
import com.projectshopbando.shopbandoapi.mappers.CategoryMapper;
import com.projectshopbando.shopbandoapi.repositories.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public Category getCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    @Cacheable(value = "category:all")
    public List<Category> getAllCategory(){
        return categoryRepository.findAll()
                    .stream()
                    .toList();
    }

    @CacheEvict(value = "category:all", allEntries = true)
    public Category createCategory(@Valid CategoryCreateReq req){
        Category category = categoryMapper.toCategory(req);
        return categoryRepository.save(category);
    }
}
