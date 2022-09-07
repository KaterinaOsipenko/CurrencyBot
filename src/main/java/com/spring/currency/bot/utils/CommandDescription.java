package com.spring.currency.bot.utils;

import org.springframework.stereotype.Component;

@Component
public class CommandDescription {

  public static final String startDesc = "get a welcome message";

  public static final String setCurrencyDesc = "set initial and target currency";

  public static final String helpDesc = "more info";

  public static final String setAmountDesc = "set the amount you want to transfer";

  public static final String checkCurrencyDesc = "check your currency";

  public static final String getRateDesc = "get actual rate";
}
