package com.spring.currency.bot.service;

import com.spring.currency.bot.model.Currency;
import com.spring.currency.bot.model.MonobankCurrency;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CurrencyConversionServiceImpl implements CurrencyConversionService{

  @Autowired
  private MonobankServiceAPI monobankService;

  private List<MonobankCurrency> allCurrencies;

  @Override
  public double getResultCurrency(Currency initial, Currency target, Optional<Double> value) {
    double resValue;
    double ratio;
    if (value.isPresent()) {
      allCurrencies = monobankService.getAllCurrencies();
      ratio = getRatio(initial, target);
      if (initial == Currency.UAN) {
        resValue = (1 / ratio) * value.get();
      } else {
        resValue = value.get() * ratio;
      }
    } else {
      log.error("There is no value in sum field");
      return 0;
    }
    log.info("Converting of value done.");
    return resValue;
  }

  private double getRatio(Currency initial, Currency target) {
    double ratio;
    MonobankCurrency currency = getCurrency(initial, target);

    if (initial == Currency.UAN) {
      ratio = currency.getRateSell();
    } else {
      ratio = currency.getRateBuy();
    }

    return ratio;
  }

  private MonobankCurrency getCurrency(Currency initial, Currency target) {
    MonobankCurrency currency;

    if (initial == Currency.UAN) {
      currency = getOneCurrency(target, initial);
    } else if (initial == Currency.EUR || target == Currency.UAN){
      currency = getOneCurrency(initial, target);
    } else {
      currency = getOneCurrency(target, initial);
    }
    return currency;
  }

  private MonobankCurrency getOneCurrency(Currency initial, Currency target) {
    return allCurrencies.stream().filter(monobankCurrency ->
            monobankCurrency.getCurrencyCodeA() == initial.code
                && monobankCurrency.getCurrencyCodeB() == target.code)
        .findAny().get();
  }



}
