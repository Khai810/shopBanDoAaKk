package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.ProductCreateRequest;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateProductReq;
import com.projectshopbando.shopbandoapi.entities.Category;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.entities.ProductSize;
import com.projectshopbando.shopbandoapi.exception.NotFoundException;
import com.projectshopbando.shopbandoapi.mappers.ProductMapper;
import com.projectshopbando.shopbandoapi.repositories.ProductRepository;
import com.projectshopbando.shopbandoapi.repositories.ProductSizeRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

@Validated
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final ProductSizeRepository productSizeRepository;

    public Product createProduct(@Valid ProductCreateRequest request) {
        Product product = productMapper.toProduct(request);
        Category category = categoryService.getCategoryById(request.getCategoryId());
        product.setCategory(category);
        return productRepository.save(product);
    }

    public Page<Product> getAllProduct(int page, int size, String name, Long categoryId, Boolean available) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.adminFindAll(name, categoryId, available, pageable);
    }

    @Cacheable(value = "landingProducts")
    public List<Product> getLandingProduct() {
        return productRepository.findTop6ByAvailableTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    public Product getProductByIdAndAvailableTrue(Long id) {
        return productRepository.findByIdAndAvailableTrue(id)
                        .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    public Page<Product> searchProduct(int page, int size, Long categoryId
            , Long excludeId, String name, BigDecimal minPrice, BigDecimal maxPrice) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchWithConditions(categoryId, excludeId, name, minPrice, maxPrice, pageable);
    }

    @Transactional
    public void decreaseProductSizeQuantity(Long productId, String productSize, int quantity) throws BadRequestException {
        ProductSize sizeEntity = productSizeRepository.findForUpdate(productId, productSize, quantity)
                .orElseThrow(() -> new BadRequestException("Not enough stock for size " + productSize));
        sizeEntity.setQuantity(sizeEntity.getQuantity() - quantity);
    }

    @Transactional
    public void increaseProductSizeQuantity(Long productId, String size, int quantity) {
        ProductSize productSize = productSizeRepository.findByProductIdAndSize(productId, size)
                .orElseThrow(() -> new NotFoundException("Product size not found with id: " + productId + "size: " + size));
        productSize.setQuantity(productSize.getQuantity() + quantity);
    }

    @Transactional
    public Product updateProduct(Long id, UpdateProductReq request) {
        Product product = getProductById(id);
        product.getSizes().clear();
        product.getImageUrls().clear();
        productMapper.toUpdateProduct(product, request);
        product.getSizes().addAll(productMapper.toProductSizes(request.getSizes(), product));
        product.getImageUrls().addAll(productMapper.toProductImages(request.getImageUrls(), product));

        Category category = categoryService.getCategoryById(request.getCategoryId());
        product.setCategory(category);

        return productRepository.save(product);
    }

    public void disableProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
        product.setAvailable(false);
        productRepository.save(product);
    }
}
