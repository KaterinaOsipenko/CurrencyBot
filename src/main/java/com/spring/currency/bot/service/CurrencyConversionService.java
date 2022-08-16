package com.spring.currency.bot.service;

import com.spring.currency.bot.model.Currency;
import java.io.IOException;
import java.net.MalformedURLException;

public interface CurrencyConversionService {

  static CurrencyConversionService getInstance() {
    return new CurrencyConversionServiceImpl();
  }

  public double getConversionRatio(Currency initial, Currency target);

}
