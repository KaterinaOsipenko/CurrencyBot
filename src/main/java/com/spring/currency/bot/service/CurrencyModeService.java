package com.spring.currency.bot.service;

import com.spring.currency.bot.model.Currency;

public interface CurrencyModeService {

    Currency getInitialCurrency(long chatId);

    Currency getTargetCurrency(long chatId);

    void setInitialCurrency(long chatId, Currency currency);

    void setTargetCurrency(long chatId, Currency currency);
}
