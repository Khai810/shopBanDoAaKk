package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.ProductCreateRequest;
import com.projectshopbando.shopbandoapi.dtos.request.ProductSizeDTO;
import com.projectshopbando.shopbandoapi.dtos.response.ProductCardRes;
import com.projectshopbando.shopbandoapi.dtos.response.ProductResponse;
import com.projectshopbando.shopbandoapi.entities.Product;
import com.projectshopbando.shopbandoapi.entities.ProductImage;
import com.projectshopbando.shopbandoapi.entities.ProductSize;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toProduct (ProductCreateRequest request);


    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "imageUrls", expression = "java(mapImages(product.getImageUrls()))")
    @Mapping(target = "sizes", expression = "java(mapSizes(product.getSizes()))")
    ProductResponse toProductResponse (Product product);

    ProductCardRes toProductCardRes(Product product);

    List<ProductCardRes> toProductCardResList(List<Product> products);

//    List<ProductResponse> toProductResponseList (List<Product> products);

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

    default List<String> mapImages(List<ProductImage> images) {
        return images == null ? null : images.stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList());
    }

    default List<ProductSizeDTO> mapSizes(List<ProductSize> sizes) {
        return sizes == null ? null : sizes.stream().map(size -> {
            ProductSizeDTO dto = new ProductSizeDTO();
            dto.setSize(size.getSize());
            dto.setQuantity(size.getQuantity());
            return dto;
        }).collect(Collectors.toList());
    }
}
