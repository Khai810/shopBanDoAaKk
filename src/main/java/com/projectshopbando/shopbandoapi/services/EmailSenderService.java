package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.entities.OnlineOrder;
import com.projectshopbando.shopbandoapi.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendOrderConfirmationEmail(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("aakkfashionshop@gmail.com");
        message.setTo(((OnlineOrder) order).getEmail());
        message.setSubject("Đơn hàng của bạn đã được xác nhận!");
        message.setText("Chào bạn,\n\n" +
                "Cảm ơn bạn đã đặt hàng tại AaKk Fashion Shop. \n" +
                "Đơn hàng của bạn với mã " + order.getId() + " đã được xác nhận.\n" +
                "Tổng số tiền: " + order.getTotalAmount() + " VND.\n\n" +
                "Bạn có thể tra cứu trạng thái đơn hàng tại: \n" +
                frontendUrl + "/order-tracking/" + order.getId() +"\n\n" +
                "Chúng tôi sẽ thông báo cho bạn khi đơn hàng được giao.\n\n" +
                "Trân trọng,\n" +
                "AaKk Fashion Shop");

        mailSender.send(message);
    }
}
