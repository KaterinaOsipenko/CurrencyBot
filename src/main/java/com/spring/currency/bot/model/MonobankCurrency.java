package com.spring.currency.bot.model;

import lombok.Getter;

@Getter
public class MonobankCurrency {

  private int currencyCodeA;

  private int currencyCodeB;

  private long date;

  private double rateSell;

  private double rateBuy;

  private double rateCross;

  @Override
  public String toString() {
    if (rateCross == 0) {
      return Currency.findByCode(currencyCodeA).name() + " to " + Currency.findByCode(currencyCodeB).name() + ":\n"
          + "Rate buy: " + rateBuy + "\nRate sell: " + rateSell;
    }
    return Currency.findByCode(currencyCodeA).name() + " to " + Currency.findByCode(currencyCodeB).name() + ":\n"
        + "Rate cross: " + rateCross;
  }
}
