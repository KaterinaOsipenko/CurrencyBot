package com.spring.currency.bot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface KeyboardService {

  InlineKeyboardMarkup getInlineCurrencyKeyBoard(long chatId);

  InlineKeyboardMarkup getInlineChoiceKeyBoard(long chatId);
}
