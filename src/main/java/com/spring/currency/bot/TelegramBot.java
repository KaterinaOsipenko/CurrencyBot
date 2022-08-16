package com.spring.currency.bot;

import com.spring.currency.bot.config.BotConfig;
import com.spring.currency.bot.dao.UserRepository;
import com.spring.currency.bot.model.Currency;
import com.spring.currency.bot.model.User;
import com.spring.currency.bot.service.CurrencyConversionService;
import com.spring.currency.bot.service.CurrencyModeService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup.InlineKeyboardMarkupBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final List<BotCommand> commandList = new ArrayList<>();
    private final CurrencyModeService currencyModeService = CurrencyModeService.getService();

    private final CurrencyConversionService currencyConversionService = CurrencyConversionService.getInstance();

    private final String HELP_TEXT = "If you want to start conversation with bot write /start\n"
        + "If you would like to set initial and target currency write /set_initial_target_currency\n"
        + "If you would like to set the amount write /set_amount\n"
        + "Make note: the default value of initial currency is UAN, the default value of target currency is USD!";

    @Autowired
    private UserRepository repository;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        log.info("Bot creation");
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
    private void setCommands() {
        this.commandList.add(new BotCommand("/start", "get a welcome message"));
        this.commandList.add(new BotCommand("/set_initial_target_currency", "set initial and target currency"));
        this.commandList.add(new BotCommand("/help", "more info"));
        this.commandList.add(new BotCommand("/set_amount", "set the amount you want to transfer"));
        try {
            execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
            log.info("Set commands to the bot");
        } catch (TelegramApiException e) {
            log.error("Set commands: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void handleCallback(CallbackQuery callback) {
        Message message = callback.getMessage();
        String[] param = callback.getData().split(":");
        String action = param[0];
        Currency currency = Currency.valueOf(param[1].trim());
        System.out.println(currency);
        switch (action) {
            case "INITIAL" -> currencyModeService.setInitialCurrency(message.getChatId(), currency);
            case "TARGET" -> currencyModeService.setTargetCurrency(message.getChatId(), currency);
            default -> log.error("Something went wrong in handle callback query method!");
        }
        try {
            execute(
                EditMessageReplyMarkup.builder()
                    .chatId(message.getChatId().toString())
                    .messageId(message.getMessageId())
                    .replyMarkup(getInlineKeyBoard(message.getChatId()))
                    .build());
        } catch (TelegramApiException e) {
            log.error("Handle Callback: " + e.getMessage());
        }
        log.info("Handle callback query from keyboard.");
    }

    private void handleMessage(Message message) {
        if (message.hasText() && message.hasEntities()) {
//            if (message.getEntities() == null) {
//                sendMessage(message.getChatId(), "It isn`t a command!", message.getChat().getUserName());
//            } else {
                Optional<MessageEntity> messageEntity = message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
                if (messageEntity.isPresent()) {
                    String command = message.getText().substring(messageEntity.get().getOffset(), messageEntity.get().getLength());
                    switch (command) {
                        case "/start" -> {
                            registerUser(message);
                            sendMessage(message.getChatId(), "Hello, " + message.getChat().getUserName() + ", welcome to the currency bot!" + EmojiParser.parseToUnicode(":wave:"), message.getChat().getUserName());
                            sendMessage(message.getChatId(), "Choose the initial and target currency:", getInlineKeyBoard(message.getChatId()), message.getChat().getUserName());
                            sendMessage(message.getChatId(), "Enter the sum: ", message.getChat().getUserName());
                        }
                        case "/help" ->
                            sendMessage(message.getChatId(), HELP_TEXT, message.getChat().getUserName());
                        case "/set_initial_target_currency" -> {
                            sendMessage(message.getChatId(), "Choose the initial and target currency:", getInlineKeyBoard(message.getChatId()), message.getChat().getUserName());
                        }
                        case "/set_amount" ->
                            sendMessage(message.getChatId(), "Enter the sum: ", message.getChat().getUserName());
                        default ->
                            sendMessage(message.getChatId(), "Sorry, command was not recognized. PLease, choose another one.", message.getChat().getUserName());
                    }
                }
            }
        if (message.hasText()) {
            String messageText = message.getText();
            Optional<Double> value = parseDouble(messageText);
            Currency initialCurrency = currencyModeService.getInitialCurrency(message.getChatId());
            Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
            double ratio = currencyConversionService.getConversionRatio(initialCurrency, targetCurrency);
            if (value.isPresent()) {
                    sendMessage(message.getChatId(), String.format(
                        "%4.2f %s is %4.2f %s",
                        value.get(), initialCurrency, (value.get() * ratio), targetCurrency), message.getChat().getUserName());
            }
        }
        log.info("Handle message.");
    }

    private Optional<Double> parseDouble(String messageText) {
        try {
            return Optional.of(Double.valueOf(messageText));
        } catch (Exception e) {
            log.error("Error parsing double: " + e.getMessage());
            return Optional.empty();
        }
    }

    private void registerUser(Message message) {
        if(repository.findById(message.getChatId()).isEmpty()) {
            long chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setUsername(chat.getUserName());
            user.setRegisterDate(new Timestamp(System.currentTimeMillis()));

            repository.save(user);
            log.info("Registered user with username: " + user);
        }
    }

    private InlineKeyboardMarkup getInlineKeyBoard(long chatId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Currency initialCurrency = currencyModeService.getInitialCurrency(chatId);
        Currency targetCurrency = currencyModeService.getTargetCurrency(chatId);
        for (Currency currency : Currency.values()) {
            buttons.add(Arrays.asList(
                InlineKeyboardButton.builder().text(getRealCurrencyButton(initialCurrency, currency)).callbackData("INITIAL: " + currency).build(),
                InlineKeyboardButton.builder().text(getRealCurrencyButton(targetCurrency, currency)).callbackData("TARGET: " + currency).build()));
        }

        return new InlineKeyboardMarkup(buttons);
    }

    private String getRealCurrencyButton(Currency saved, Currency currency) {
        return saved == currency ? currency.name() + EmojiParser.parseToUnicode(":money_with_wings:") : currency.name();
    }

    private void sendMessage(long chatId, String text, ReplyKeyboard keyboard, String username) {
        SendMessage sender = new SendMessage();
        sender.setChatId(chatId);
        sender.setText(text);
        sender.setReplyMarkup(keyboard);
        try {
            execute(sender);
        } catch (TelegramApiException ex) {
            log.error("Send message exception: " + ex.getMessage());
        }
        log.info("Replied to user: " + username);
    }

    private void sendMessage(long chatId, String text, String username) {
        SendMessage sender = new SendMessage();
        sender.setChatId(chatId);
        sender.setText(text);
        try {
            execute(sender);
        } catch (TelegramApiException ex) {
            log.error("Send message exception: " + ex.getMessage());
        }
        log.info("Replied to user: " + username);
    }
}
