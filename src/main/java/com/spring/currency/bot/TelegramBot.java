package com.spring.currency.bot;

import com.spring.currency.bot.config.BotConfig;
import com.spring.currency.bot.handler.MessageHandler;
import com.spring.currency.bot.model.CallbackTitle;
import com.spring.currency.bot.model.CommandTitle;
import com.spring.currency.bot.model.Currency;
import com.spring.currency.bot.service.CommandService;
import com.spring.currency.bot.service.CurrencyConversionService;
import com.spring.currency.bot.service.CurrencyModeService;
import com.spring.currency.bot.service.KeyboardService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

@Slf4j
@Component
public class TelegramBot extends SpringWebhookBot {

    private final BotConfig botConfig;
    @Autowired
    private CommandService commandService;
    @Autowired
    private MessageHandler messageHandler;
    @Autowired
    private CurrencyModeService currencyModeService;
    @Autowired
    private CurrencyConversionService currencyConversionService;
    @Autowired
    private KeyboardService keyboardService;

    public TelegramBot(BotConfig botConfig, CommandService commandService, SetWebhook setWebhook) {
        super(setWebhook);
        this.botConfig = botConfig;
        this.commandService = commandService;
        log.info("TelegramBot: Bot creation");
        setCommands();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public String getBotPath() {
        return botConfig.getPath();
    }
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        log.info("TelegramBot: onWebhookUpdateReceived ...");
        if (update.hasMessage()) {
           return messageHandler.handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            return handleCallback(update.getCallbackQuery());
        }
        return null;
    }

    public BotApiMethod<?>  handleCallback(CallbackQuery callback) {
        Currency currency;
        Message message = callback.getMessage();
        String[] param = callback.getData().split(":");
        CallbackTitle action = CallbackTitle.valueOf(param[0]);

        switch (action) {
            case INITIAL:
                currency = Currency.valueOf(param[1].trim());
                currencyModeService.setInitialCurrency(message.getChatId(), currency);
                return editMessageReplyMarkup(message);
            case TARGET:
                currency = Currency.valueOf(param[1].trim());
                currencyModeService.setTargetCurrency(message.getChatId(), currency);
                return editMessageReplyMarkup(message);
            case RATE:
                return getUpdateForCommand(CommandTitle.get_rate, message);

            case CONVERSION:
                return getUpdateForCommand(CommandTitle.set_initial_target_currency, message);

            case CALCULATE:
                return SendMessage.builder().chatId(message.getChatId())
                    .text(currencyConversionService.getRate(
                        currencyModeService.getInitialCurrency(message.getChatId()),
                        currencyModeService.getTargetCurrency(message.getChatId()))).build();

            default:
                log.error("TelegramBot: Something went wrong in handle callback query method!");
        }
        return null;
    }
    private BotApiMethod<?> getUpdateForCommand(CommandTitle command, Message incomingMsg) {
        Update update = new Update();
        Message message = new Message();
        MessageEntity messageEntity = new MessageEntity();

        messageEntity.setType("bot_command");
        messageEntity.setOffset(0);
        messageEntity.setLength(command.label.length());
        messageEntity.setText(command.label);

        message.setChat(incomingMsg.getChat());
        message.setText(command.label);
        message.setEntities(List.of(messageEntity));

        update.setMessage(message);
        return onWebhookUpdateReceived(update);
    }

    public BotApiMethod<?> editMessageReplyMarkup(Message message) {
        return
            EditMessageReplyMarkup.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .replyMarkup(keyboardService.getInlineCurrencyKeyBoard(message.getChatId()))
                .build();
    }

    private void setCommands() {
        try {
            execute(new SetMyCommands(commandService.getListOfBotCommands(), new BotCommandScopeDefault(), null));
            log.info("TelegramBot: Set commands to the bot");
        } catch (TelegramApiException e) {
            log.error("TelegramBot: Set commands: " + e.getMessage());
        }
    }

}
