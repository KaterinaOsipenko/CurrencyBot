package com.spring.currency.bot.service;

import com.spring.currency.bot.model.Currency;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.stereotype.Service;

@Service
public class CurrencyConversionServiceImpl implements CurrencyConversionService{

  @Override
  public double getConversionRatio(Currency initial, Currency target)  {
    return 0;
  }
}
