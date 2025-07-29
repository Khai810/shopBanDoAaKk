package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.response.OrderProductRes;
import com.projectshopbando.shopbandoapi.dtos.response.OrderResPayload;
import com.projectshopbando.shopbandoapi.dtos.response.OrderResponse;
import com.projectshopbando.shopbandoapi.entities.Order;
import com.projectshopbando.shopbandoapi.entities.OrderProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class})
public interface OrderMapper {

    @Mapping(target = "orderedProduct", source = "orderedProduct")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderProductRes toOrderProductRes(OrderProduct orderProduct);

    List<OrderProductRes> toOrderProductResponseList(List<OrderProduct> orderedProduct);

    List<OrderResponse> toOrderResponseList(List<Order> order);

    @Mapping(target = "order", expression = "java(toOrderResponse(order))")
    OrderResPayload toOrderResPayload(Order order);

    List<OrderResPayload> toOrderResPayloadList(List<Order> order);
}
