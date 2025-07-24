package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.ProductCreateRequest;
import com.projectshopbando.shopbandoapi.dtos.response.ProductResponse;
import com.projectshopbando.shopbandoapi.entities.Category;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.mappers.ProductMapper;
import com.projectshopbando.shopbandoapi.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = productMapper.toProduct(request);
        Category category = categoryService.getCategoryById(request.getCategoryId());
        product.setCategory(category);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public List<ProductResponse> getAllProduct() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toProductResponse)
                .toList();
    }
}
