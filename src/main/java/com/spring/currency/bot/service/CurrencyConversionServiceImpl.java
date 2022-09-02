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

  @Override
  public String getRate(Currency initial, Currency target) {
    log.info("CurrencyConversionServiceImpl: getting rate.");
    allCurrencies = monobankService.getAllCurrencies();

    Optional<MonobankCurrency> currency = getCurrency(initial, target);
    if (currency.isEmpty()) {
      currency = getCurrency(target, initial);
    }

    if (currency.isPresent()) {
      MonobankCurrency monobankCurrency = currency.get();
      return monobankCurrency.toString();
    } else {
      log.error("CurrencyConversionServiceImpl: there is no value currencies.");
      return "There is no data for these currencies.";
    }
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
    double ratio = 0;
    Optional<MonobankCurrency> currency = getCurrency(initial, target);

    if (currency.isPresent()) {
      MonobankCurrency monobankCurrency = currency.get();
      if (monobankCurrency.getRateCross() != 0) {
        ratio = monobankCurrency.getRateCross();
      } else if (initial == Currency.UAN) {
        ratio = monobankCurrency.getRateSell();
      } else {
        ratio = monobankCurrency.getRateBuy();
      }
    } else {
      log.error("CurrencyConversionServiceImpl: there is no value in currency.");
    }

    log.info("CurrencyConversionServiceImpl: receiving ratio from currency entity");
    return ratio;
  }

  private Optional<MonobankCurrency> getCurrency(Currency initial, Currency target) {
    Optional<MonobankCurrency> currency;

    if (target == Currency.UAN) {
      currency = getOneCurrency(initial, target);
    } else {
      currency = getOneCurrency(target, initial);
    }

    return currency;

  }

  private Optional<MonobankCurrency> getOneCurrency(Currency initial, Currency target) {
    log.info("CurrencyConversionServiceImpl: receiving required entity from list of all entities.");
    return allCurrencies.stream().filter(monobankCurrency ->
            monobankCurrency.getCurrencyCodeA() == initial.code
                && monobankCurrency.getCurrencyCodeB() == target.code)
        .findAny();
  }



}
