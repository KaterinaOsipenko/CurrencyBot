package com.spring.currency.bot.service;

import com.spring.currency.bot.model.CallbackTitle;
import com.spring.currency.bot.model.CommandTitle;
import com.spring.currency.bot.model.Currency;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


@Slf4j
@Service
public class KeyboardServiceImpl implements KeyboardService {

  @Autowired
  private CommandService commandService;

  @Autowired
  private CurrencyModeService currencyModeService;


  @Override
  public InlineKeyboardMarkup getInlineCurrencyKeyBoard(long chatId) {
    List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
    Currency initialCurrency = currencyModeService.getInitialCurrency(chatId);
    Currency targetCurrency = currencyModeService.getTargetCurrency(chatId);
    for (Currency currency : Currency.values()) {
      buttons.add(Arrays.asList(
          InlineKeyboardButton.builder().text(getRealCurrencyButton(initialCurrency, currency)).callbackData(CallbackTitle.INITIAL.name() + ": " + currency).build(),
          InlineKeyboardButton.builder().text(getRealCurrencyButton(targetCurrency, currency)).callbackData(CallbackTitle.TARGET.name() + ": " + currency).build()));
    }
    if (this.commandService.getCurrentCommand().equals(CommandTitle.get_rate.label)) {
      buttons.add(Collections.singletonList(
          InlineKeyboardButton.builder().text("Calculate").callbackData(CallbackTitle.CALCULATE.name()).build()));
    }

    return new InlineKeyboardMarkup(buttons);
  }

  @Override
  public InlineKeyboardMarkup getInlineChoiceKeyBoard(long chatId) {
    List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
    buttons.add(Arrays.asList(
        InlineKeyboardButton.builder().text("Find out the current rate").callbackData(
            CallbackTitle.RATE.name()).build(),
        InlineKeyboardButton.builder().text("Convert currencies").callbackData(CallbackTitle.CONVERSION.name()).build()));
    return new InlineKeyboardMarkup(buttons);
  }

  private String getRealCurrencyButton(Currency saved, Currency currency) {
    return saved == currency ? currency.name() + EmojiParser.parseToUnicode(":money_with_wings:") : currency.name();
  }
}
