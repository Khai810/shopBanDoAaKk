package com.projectshopbando.shopbandoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class ShopBanDoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopBanDoApiApplication.class, args);
        System.out.println("ShopBanDo API Application Started");
    }

}
