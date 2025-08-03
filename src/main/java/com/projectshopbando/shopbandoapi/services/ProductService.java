package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.ProductCreateRequest;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Validated
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final ProductSizeRepository productSizeRepository;
    private final RedisTemplate<String, Integer> redisTemplate;

    public Product createProduct(@Valid ProductCreateRequest request) {
        Product product = productMapper.toProduct(request);
        Category category = categoryService.getCategoryById(request.getCategoryId());
        product.setCategory(category);
        return productRepository.save(product);
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll()
                .stream().toList();
    }

    @Cacheable(value = "landingProducts")
    public List<Product> getLandingProduct() {
        return productRepository.findTop6ByAvailableTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    public Product getProductById(Long id) {
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
        String redisKey = String.format("product:%s:size:%s", productId, productSize);
        Integer sizeQuantity = redisTemplate.opsForValue().get(redisKey);
        if(sizeQuantity != null){
            if(sizeQuantity < quantity){
                throw new BadRequestException("Quantity exceeded");
            }
            redisTemplate.opsForValue().decrement(redisKey, quantity);
        }
        ProductSize sizeEntity = productSizeRepository.findForUpdate(productId, productSize, quantity)
                .orElseThrow(() -> new BadRequestException("Not enough stock for size" + productSize));
        sizeEntity.setQuantity(sizeEntity.getQuantity() - quantity);
        redisTemplate.opsForValue().set(redisKey, sizeEntity.getQuantity(), Duration.ofMinutes(2));
    }

    @Transactional
    public void increaseProductSizeQuantity(Long productId, String size, int quantity) throws BadRequestException {
        ProductSize productSize = productSizeRepository.findByProductIdAndSize(productId, size)
                .orElseThrow(() -> new NotFoundException("Product size not found with id: " + productId + "size: " + size));
        productSize.setQuantity(productSize.getQuantity() + quantity);
    }
}
