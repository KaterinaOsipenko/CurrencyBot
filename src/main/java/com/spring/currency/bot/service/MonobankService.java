package com.spring.currency.bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.currency.bot.connector.HttpConnector;
import com.spring.currency.bot.exception.TooManyRequestException;
import com.spring.currency.bot.model.MonobankCurrency;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.internal.Errors.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
public class MonobankService implements MonobankServiceAPI{

  @Autowired
  private HttpConnector connector;

  @Cacheable(value = "getAllCurrenciesFromCache")
  public List<MonobankCurrency> getAllCurrencies() {
    ResponseEntity<MonobankCurrency[]> currencies = connector.getCurrencies();
    if (!currencies.getStatusCode().is2xxSuccessful()) {
      handleException(currencies);
    }
    log.info("Receiving all currencies from API response.");
    return Arrays.asList(currencies.getBody());
  }



  private void handleException(ResponseEntity response) {
    if (response.getStatusCode() == HttpStatus.valueOf(429)) {
      log.error("Throw 429 exception from connector.");
      throw new TooManyRequestException(response.getStatusCode());
    }
    log.error("Throw runtime exception from connector.");
    throw new RuntimeException(String.valueOf(response.getStatusCode()));
  }
}
