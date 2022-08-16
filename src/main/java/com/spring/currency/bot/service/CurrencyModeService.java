package com.spring.currency.bot.service;

import com.spring.currency.bot.model.Currency;

public interface CurrencyModeService {

    static CurrencyModeService getService() {
        return new HashMapCurrencyModeService();
    }

    Currency getInitialCurrency(long chatId);

    Currency getTargetCurrency(long chatId);

    void setInitialCurrency(long chatId, Currency currency);

    void setTargetCurrency(long chatId, Currency currency);
}
