package com.spring.currency.bot.model;

import com.spring.currency.bot.service.MonobankService;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class MonobankCurrency {

  private int currencyCodeA;

  private int currencyCodeB;

  private long date;

  private double rateSell;

  private double rateBuy;

  private double rateCross;

}
