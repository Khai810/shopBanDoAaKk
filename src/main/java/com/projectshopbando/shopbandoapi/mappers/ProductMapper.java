package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.ProductCreateRequest;
import com.projectshopbando.shopbandoapi.dtos.request.ProductSizeDTO;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateProductReq;
import com.projectshopbando.shopbandoapi.dtos.response.ProductAdminResponse;
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

    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    @Mapping(target = "category", ignore = true)
    void toUpdateProduct(@MappingTarget Product product, UpdateProductReq request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "imageUrls", expression = "java(mapImages(product.getImageUrls()))")
    @Mapping(target = "sizes", expression = "java(mapSizes(product.getSizes()))")
    ProductResponse toProductResponse (Product product);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "imageUrls", expression = "java(mapImages(product.getImageUrls()))")
    @Mapping(target = "sizes", expression = "java(mapSizes(product.getSizes()))")
    ProductAdminResponse toProductAdminResponse (Product product);

    @Mapping(target = "categoryId", source = "category.id")
    ProductCardRes toProductCardRes(Product product);

    List<ProductCardRes> toProductCardResList(List<Product> products);

    // After toProduct
    @AfterMapping
    default void linkRelations (@MappingTarget Product product, ProductCreateRequest request) {
        if (request.getImageUrls() != null) {
            product.setImageUrls(
                    toProductImages(request.getImageUrls(), product));
        }
        if (request.getSizes() != null) {
            product.setSizes(
                    toProductSizes(request.getSizes(), product));
        }
    }

    default List<ProductImage> toProductImages (List<String> imageUrls, Product product) {
        return  imageUrls.stream()
                .map(url -> ProductImage.builder()
                        .product(product)
                        .url(url)
                        .build())
                .toList();
    }

    default List<ProductSize> toProductSizes(List<ProductSizeDTO> sizeDTO, Product product) {
        return sizeDTO.stream().map(
                size -> ProductSize.builder()
                        .size(size.getSize())
                        .quantity(size.getQuantity())
                        .product(product)
                        .build())
                .toList();
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
