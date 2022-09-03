package com.spring.currency.bot.handler;

import com.spring.currency.bot.model.CommandTitle;
import com.spring.currency.bot.model.Currency;
import com.spring.currency.bot.service.CommandService;
import com.spring.currency.bot.service.CurrencyConversionService;
import com.spring.currency.bot.service.CurrencyModeService;
import com.spring.currency.bot.service.KeyboardService;
import com.spring.currency.bot.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

@Component
@Slf4j
public class MessageHandlerImpl implements MessageHandler {
  @Autowired
  private UserService userService;
  @Autowired
  private CommandService commandService;
  @Autowired
  private KeyboardService keyboardService;
  @Autowired
  private CurrencyConversionService currencyConversionService;
  @Autowired
  private CurrencyModeService currencyModeService;

  private final String HELP_TEXT = "If you want to start conversation with bot write /start" +
        "If you would like to set initial and target currency write /set_initial_target_currency" +
        "If you would like to set the amount write /set_amount" +
        "If you want to find out your current mode of currencies write /check_initial_target_currency" +
       " Make note: the default value of initial currency is UAN, the default value of target currency is USD!";


  @Override
  public BotApiMethod<?> handleMessage(Message message) {
    if (message.hasText() && message.hasEntities()) {
      Optional<MessageEntity> messageEntity = message.getEntities().stream()
          .filter(e -> "bot_command".equals(e.getType())).findFirst();
      if (messageEntity.isPresent()) {
        String command = message.getText().substring(1);
        CommandTitle title = CommandTitle.valueOf(command);
        switch (title) {
          case start -> {
            userService.registerUser(message);
            commandService.setCurrentCommand(CommandTitle.start);
           return SendMessage.builder().chatId(message.getChatId()).text( "Hello, " + message.getChat().getFirstName()
                + ", welcome to the currency bot!" + EmojiParser.parseToUnicode(":wave:") +
                "\nWhat do you want to do?")
                .replyMarkup(keyboardService.getInlineChoiceKeyBoard(message.getChatId())).build();
          }
          case help -> {
            commandService.setCurrentCommand(CommandTitle.help);
           return SendMessage.builder().chatId(message.getChatId()).text(HELP_TEXT).build();
          }
          case check_initial_target_currency -> {
            commandService.setCurrentCommand(
                CommandTitle.check_initial_target_currency);

           return SendMessage.builder().chatId(message.getChatId()).text("Your initial currency is "
                + currencyModeService.getInitialCurrency(
                message.getChatId()) + " and target currency is "
                + currencyModeService.getTargetCurrency(message.getChatId())).build();
          }
          case set_initial_target_currency -> {
            commandService.setCurrentCommand(
                CommandTitle.set_initial_target_currency);

            commandService.setCurrentCommand(CommandTitle.set_amount);

           return SendMessage.builder().chatId(message.getChatId()).text("Choose the initial, target currency and enter the sum:")
                .replyMarkup(keyboardService.getInlineCurrencyKeyBoard(message.getChatId()))
                .build();
          }
          case set_amount -> {
            commandService.setCurrentCommand(CommandTitle.set_amount);

           return SendMessage.builder().chatId(message.getChatId()).text("Enter the sum: ").build();
          }
          case get_rate -> {
            commandService.setCurrentCommand(CommandTitle.get_rate);

            return SendMessage.builder().chatId(message.getChatId()).text("What currency you are interested in?")
                    .replyMarkup(keyboardService.getInlineCurrencyKeyBoard(message.getChatId())).build();
          }
          default -> SendMessage.builder().chatId(message.getChatId()).text("Sorry, command was not recognized. PLease, choose another one.").build();
        }
      }
    } else if (message.hasText()) {
      return SendMessage.builder().chatId(message.getChatId()).text(handleMessageDigit(message)).build();
    }
    log.info("TelegramBot: Handle message.");
    return null;
  }

  private String handleMessageDigit(Message message) {
    String messageText = message.getText();
    double resValue;
    String messageToSend;
    Optional<Double> value = parseDouble(messageText);
    if (value.isPresent()) {
      Currency initialCurrency = currencyModeService.getInitialCurrency(message.getChatId());
      Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
      try {
        resValue = currencyConversionService.getResultCurrency(initialCurrency,
            targetCurrency, value);
        messageToSend = String.format(
            "%4.2f %s is %4.2f %s",
            value.get(), initialCurrency, resValue, targetCurrency);
      } catch (HttpClientErrorException ex) {
        messageToSend = "Please, retry after a few seconds.";
        log.error("TelegramBot: Runtime exception: " + ex.getMessage());
      }
    } else {
      messageToSend = "Please, write only a digit number!";
    }
    return messageToSend;
  }

  private Optional<Double> parseDouble(String messageText) {
    if (messageText.matches("\\d{1,10}(\\.\\d*)?")) {
      try {
        return Optional.of(Double.valueOf(messageText));
      } catch (Exception e) {
        log.error("TelegramBot: Error parsing double: " + e.getMessage());
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

}

