package com.spring.currency.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableCaching
public class SpingCurrencyBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpingCurrencyBotApplication.class, args);
    }

}
