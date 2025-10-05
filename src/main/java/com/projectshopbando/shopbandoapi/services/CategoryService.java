package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.CategoryCreateReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateCategoryReq;
import com.projectshopbando.shopbandoapi.entities.Category;
import com.projectshopbando.shopbandoapi.exception.NotFoundException;
import com.projectshopbando.shopbandoapi.mappers.CategoryMapper;
import com.projectshopbando.shopbandoapi.repositories.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // For admin
    public Category getCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    // For user
    public Category getCategoryByIdAndIsDisabledFalse(Long id){
        return categoryRepository.findByIdAndIsDisabledFalse(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    // For user
    @Cacheable(value = "category:all")
    public List<Category> getAllCategory(){
        return categoryRepository.findAllByIsDisabledFalse().orElseThrow();
    }

    public Page<Category> getAllCategoryAdmin(int page, int size, String search){
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepository.findByNameContaining(search, pageable);
    }

    @CacheEvict(value = "category:all", allEntries = true)
    public Category createCategory(@Valid CategoryCreateReq req){
        Category category = categoryMapper.toCategory(req);
        return categoryRepository.save(category);
    }

    @CacheEvict(value = "category:all", allEntries = true)
    public Category updateCategory(Long id, @Valid UpdateCategoryReq req) {
        Category category = getCategoryById(id);
        categoryMapper.updateCategory(category, req);
        if(req.getIsDisabled().equals(true)){
            category.getProducts().forEach(product -> product.setAvailable(false));
        }
        return categoryRepository.save(category);
    }
}
