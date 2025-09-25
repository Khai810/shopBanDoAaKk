package com.projectshopbando.shopbandoapi.mappers;

import com.projectshopbando.shopbandoapi.dtos.request.CreateOrderReq;
import com.projectshopbando.shopbandoapi.dtos.response.OrderProductRes;
import com.projectshopbando.shopbandoapi.dtos.response.OrderDto;
import com.projectshopbando.shopbandoapi.entities.OfflineOrder;
import com.projectshopbando.shopbandoapi.entities.OnlineOrder;
import com.projectshopbando.shopbandoapi.entities.Order;
import com.projectshopbando.shopbandoapi.entities.OrderProduct;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class})
public interface OrderMapper {

    @Mapping(target = "orderedProducts", source = "orderedProducts")
    OrderDto toOrderDto(Order order);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    Order toOrder(CreateOrderReq request);

    @ObjectFactory
    default Order resolveOrder(CreateOrderReq req) {
        if ("ONLINE".equalsIgnoreCase(req.getType())) {
            return new OnlineOrder();
        } else if ("OFFLINE".equalsIgnoreCase(req.getType())) {
            return new OfflineOrder();
        }
        throw new IllegalArgumentException("Unknown order type: " + req.getType());
    }

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderProductRes toOrderProductRes(OrderProduct orderProduct);

    List<OrderProductRes> toOrderProductResponseList(List<OrderProduct> orderedProduct);

    @AfterMapping
    default void fillSubtypeFields(Order order, @MappingTarget OrderDto dto) {
        if (order instanceof OfflineOrder offline) {
            dto.setStaffName(offline.getStaff().getFullName());
            dto.setStore(offline.getStaff().getStore());
        } else if (order instanceof OnlineOrder online) {
            dto.setEmail(online.getEmail());
            dto.setAddress(online.getAddress());
            dto.setShippingFee(online.getShippingFee());
        }
    }
}
