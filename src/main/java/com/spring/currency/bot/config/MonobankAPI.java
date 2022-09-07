package com.spring.currency.bot.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class MonobankAPI {

  @Value("${monobank.token}")
  private String token;

  @Value("${monobank.url}")
  private String url;

}
