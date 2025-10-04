package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.config.VnPayConfig;
import com.projectshopbando.shopbandoapi.dtos.response.IpnRes;
import com.projectshopbando.shopbandoapi.entities.Order;
import com.projectshopbando.shopbandoapi.enums.IpnResConst;
import com.projectshopbando.shopbandoapi.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.projectshopbando.shopbandoapi.config.VnPayConfig.secretKey;

@RequiredArgsConstructor
@Service
public class VNPayIpnHandler {
    private final OrderService orderService;
    private final EmailSenderService emailSenderService;

    public IpnRes ipnHandler(Map<String, String> params) {
        try {
            String vnp_SecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();

            var itr = fieldNames.iterator();
            while (itr.hasNext()) {
                var fieldName = itr.next();
                var fieldValue = params.get(fieldName);
                if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append("=");
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        hashData.append("&");
                    }
                }
            }
            //Check hash

            String secureHash = VnPayConfig.hmacSHA512(secretKey, hashData.toString());
            if (!secureHash.equals(vnp_SecureHash)) {
                return IpnResConst.INVALID_CHECKSUM;
            }
            String responseCode = params.get("vnp_ResponseCode");
            String orderId = params.get("vnp_TxnRef");
            BigDecimal amount = BigDecimal.valueOf(Long.parseLong(params.get("vnp_Amount"))).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

            if (!orderService.checkIfOrderExists(orderId)) {
                return IpnResConst.ORDER_NOTFOUND;
            }

            Order order = orderService.getOrderById(orderId);

            if (!order.getTotalAmount().equals(amount)) {
                return IpnResConst.INVALID_AMOUNT;
            }
            if (!order.getStatus().equals(OrderStatus.UNPAID)) {
                return IpnResConst.ORDER_COMFIRMED;
            }

            if (responseCode.equals("00")) {
                orderService.updateOrderStatus(order.getId(), OrderStatus.PREPARING);
                emailSenderService.sendOrderConfirmationEmail(order);
                return IpnResConst.SUCCESS;
            } else {
                orderService.cancelOrder(orderId);
                return IpnResConst.SUCCESS;
            }
        } catch (Exception e) {
            return IpnResConst.UNKNOWN_ERROR;
        }
    }
}
