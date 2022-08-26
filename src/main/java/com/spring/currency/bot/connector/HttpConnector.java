package com.spring.currency.bot.connector;

import com.spring.currency.bot.config.MonobankAPI;
import com.spring.currency.bot.model.MonobankCurrency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class HttpConnector {

  @Autowired
  private MonobankAPI monobankAPI;

  public ResponseEntity<MonobankCurrency[]> getCurrencies() {
    log.info("Connecting to API...");
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<MonobankCurrency[]> response;
    try {
      response = restTemplate.getForEntity(monobankAPI.getUrl(),
          MonobankCurrency[].class);
    } catch (HttpClientErrorException ex) {
      response = new ResponseEntity<>(ex.getStatusCode());
      log.error("RestTemplate error during connection: " + ex.getMessage());
    }
    return response;
  }


}
