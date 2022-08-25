package com.spring.currency.bot.service;

import com.spring.currency.bot.model.MonobankCurrency;
import java.util.List;

public interface MonobankServiceAPI {
  public List<MonobankCurrency> getAllCurrencies();
}
