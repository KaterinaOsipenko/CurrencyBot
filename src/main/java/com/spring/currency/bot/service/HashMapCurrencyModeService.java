package com.spring.currency.bot.service;

import com.spring.currency.bot.model.Currency;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HashMapCurrencyModeService implements CurrencyModeService{

    private final Map<Long, Currency> initialCurrency = new HashMap<>();
    private final Map<Long, Currency> targetCurrency = new HashMap<>();

    public HashMapCurrencyModeService() {
        log.info("HashMapCurrencyService created!");
    }

    @Override
    public Currency getInitialCurrency(long chatId) {
        return initialCurrency.getOrDefault(chatId, Currency.USD);
    }

    @Override
    public Currency getTargetCurrency(long chatId) {
        return targetCurrency.getOrDefault(chatId, Currency.USD);
    }

    @Override
    public void setInitialCurrency(long chatId, Currency currency) {
        initialCurrency.put(chatId, currency);
    }

    @Override
    public void setTargetCurrency(long chatId, Currency currency) {
        targetCurrency.put(chatId, currency);
    }
}
