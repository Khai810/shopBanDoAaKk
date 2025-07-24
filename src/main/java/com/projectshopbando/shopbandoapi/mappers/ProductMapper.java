package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.ProductCreateRequest;
import com.projectshopbando.shopbandoapi.dtos.request.ProductSizeDTO;
import com.projectshopbando.shopbandoapi.dtos.response.ProductResponse;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.entities.ProductImage;
import com.projectshopbando.shopbandoapi.entities.ProductSize;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toProduct (ProductCreateRequest request);


    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    ProductResponse toProductResponse (Product product);




    // After toProduct
    @AfterMapping
    default void linkRelations (@MappingTarget Product product, ProductCreateRequest request) {
        if (request.getImageUrls() != null) {
            List<ProductImage> images = request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .product(product)
                            .url(url)
                            .build())
                    .toList();
            product.setImageUrls(images);
        }
        if (request.getSizes() != null) {
            List<ProductSize> sizes = request.getSizes().stream()
                    .map(size -> ProductSize.builder()
                            .size(size.getSize())
                            .quantity(size.getQuantity())
                            .product(product)
                            .build())
                    .toList();
            product.setSizes(sizes);
        }
    }

    @AfterMapping
    default void transformRelations (@MappingTarget ProductResponse productResponse, Product product) {
        if (product.getImageUrls() != null) {
            List<String> img = product.getImageUrls().stream()
                        .map(ProductImage::getUrl)
                        .toList();
            productResponse.setImageUrls(img);
        }
        if (product.getSizes() != null) {
            List<ProductSizeDTO> sizes = product.getSizes().stream()
                    .map(size -> ProductSizeDTO.builder()
                            .size(size.getSize())
                            .quantity(size.getQuantity())
                            .build())
                    .toList();
            productResponse.setSizes(sizes);
        }
    }
}
