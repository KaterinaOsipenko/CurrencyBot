package com.spring.currency.bot.model;

import java.util.Arrays;
import java.util.Optional;

public enum Currency {
  USD(840),
  EUR(978),
  PLN(985),
  UAN(980);
  public final int code;

  Currency(int code) {
    this.code = code;
  }

  public static Currency findByCode(int code) {
    return Arrays.stream(Currency.values()).filter(currency -> currency.code == code).findFirst().get();
  }
}
