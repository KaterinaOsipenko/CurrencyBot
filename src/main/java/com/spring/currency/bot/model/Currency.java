package com.spring.currency.bot.model;

public enum Currency {
  USD(840),
  EUR(978),
  PLN(985),
  UAN(980);
  public final int code;
  Currency(int code) {
    this.code = code;
  }
}
