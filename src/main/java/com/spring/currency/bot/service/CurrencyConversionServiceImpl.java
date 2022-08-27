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
    if (value.isPresent()) {
      allCurrencies = monobankService.getAllCurrencies();
      resValue = getConvertValue(initial, target, value.get());
    } else {
      log.error("CurrencyConversionServiceImpl: There is no value in value field");
      return 0;
    }
    log.info("CurrencyConversionServiceImpl: Converting of value done.");
    return resValue;
  }

  private double getConvertValue(Currency initial, Currency target, Double value) {
    double resValue;
    double foreignCurrencyRatio;
    double foreignVal;

    if (initial != Currency.UAN && target != Currency.UAN) {
      foreignCurrencyRatio = getRatio(initial, Currency.UAN);

      foreignVal = value * foreignCurrencyRatio;

      value = foreignVal;

      initial = Currency.UAN;
    }

    double ratio = getRatio(initial, target);
    if (initial == Currency.UAN) {
      resValue = (1 / ratio) * value;
    } else {
      resValue = value * ratio;
    }
    log.info("CurrencyConversionServiceImpl: converting currency...");
    return resValue;
  }

  private double getRatio(Currency initial, Currency target) {
    double ratio;
    MonobankCurrency currency = getCurrency(initial, target);

    if (currency.getRateCross() != 0) {
      ratio = currency.getRateCross();
    } else if (initial == Currency.UAN) {
      ratio = currency.getRateSell();
    } else {
      ratio = currency.getRateBuy();
    }
    log.info("CurrencyConversionServiceImpl: receiving ratio from currency entity");
    return ratio;
  }

  private MonobankCurrency getCurrency(Currency initial, Currency target) {
    MonobankCurrency currency;

    if (target == Currency.UAN) {
      currency = getOneCurrency(initial, target);
    } else {
      currency = getOneCurrency(target, initial);
    }
    return currency;
  }

  private MonobankCurrency getOneCurrency(Currency initial, Currency target) {
    log.info("CurrencyConversionServiceImpl: receiving required entity from list of all entities.");
    return allCurrencies.stream().filter(monobankCurrency ->
            monobankCurrency.getCurrencyCodeA() == initial.code
                && monobankCurrency.getCurrencyCodeB() == target.code)
        .findAny().get();
  }



}
