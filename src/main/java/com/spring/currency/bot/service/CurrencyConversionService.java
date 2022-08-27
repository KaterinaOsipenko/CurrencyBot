package com.spring.currency.bot.service;

import com.spring.currency.bot.model.Currency;
import java.util.Optional;

public interface CurrencyConversionService {
  double getResultCurrency(Currency initial, Currency target, Optional<Double> value);

}
