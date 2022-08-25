package com.spring.currency.bot.model;

public enum Currency {
    USD(840),
    EUR(978),
    UAN(980);

    public int code;

     Currency(int code) {
        this.code = code;
    }

    public static Currency get(int code) {
         for (Currency currency : Currency.values()) {
             if (currency.code == code) {
                 return currency;
             }
         }
         return null;
    }
}
