package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.response.OrderProductRes;
import com.projectshopbando.shopbandoapi.dtos.response.OrderDto;
import com.projectshopbando.shopbandoapi.entities.Order;
import com.projectshopbando.shopbandoapi.entities.OrderProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class})
public interface OrderMapper {

    @Mapping(target = "orderedProduct", source = "orderedProduct")
    OrderDto toOrderDto(Order order);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    Order toOrder(CreateOrderReq request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderProductRes toOrderProductRes(OrderProduct orderProduct);

    List<OrderProductRes> toOrderProductResponseList(List<OrderProduct> orderedProduct);

//    List<OrderResponse> toOrderResponseList(List<Order> order);
}
