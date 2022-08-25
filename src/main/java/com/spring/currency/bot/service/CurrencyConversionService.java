package com.spring.currency.bot.service;

import com.spring.currency.bot.model.Currency;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

public interface CurrencyConversionService {


  public double getResultCurrency(Currency initial, Currency target, Optional<Double> value);

}
