package com.spring.currency.bot;

import com.spring.currency.bot.config.BotConfig;
import com.spring.currency.bot.dao.UserRepository;
import com.spring.currency.bot.model.CallbackTitle;
import com.spring.currency.bot.model.CommandTitle;
import com.spring.currency.bot.model.Currency;
import com.spring.currency.bot.model.User;
import com.spring.currency.bot.service.CommandService;
import com.spring.currency.bot.service.CurrencyConversionService;
import com.spring.currency.bot.service.CurrencyModeService;
import com.spring.currency.bot.service.KeyboardService;
import com.vdurmont.emoji.EmojiParser;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    private final CommandService commandService;

    @Autowired
    private CurrencyConversionService currencyConversionService;
    @Autowired
    private CurrencyModeService currencyModeService;
    @Autowired
    private UserRepository repository;
    @Autowired
    private KeyboardService keyboardService;

    private final String HELP_TEXT = """
        If you want to start conversation with bot write /start
        If you would like to set initial and target currency write /set_initial_target_currency
        If you would like to set the amount write /set_amount
        If you want to find out your current mode of currencies write /check_initial_target_currency
        Make note: the default value of initial currency is UAN, the default value of target currency is USD!""";

    public TelegramBot(BotConfig botConfig, CommandService commandService) {
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

    private void setCommands() {
        try {
            execute(new SetMyCommands(commandService.getListOfBotCommands(), new BotCommandScopeDefault(), null));
            log.info("TelegramBot: Set commands to the bot");
        } catch (TelegramApiException e) {
            log.error("TelegramBot: Set commands: " + e.getMessage());
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
        Currency currency;
        Message message = callback.getMessage();
        String[] param = callback.getData().split(":");
        CallbackTitle action = CallbackTitle.valueOf(param[0]);

        switch (action) {
            case INITIAL -> {
                currency = Currency.valueOf(param[1].trim());
                currencyModeService.setInitialCurrency(message.getChatId(), currency);
                editMessageReplyMarkup(message);
            }
            case TARGET -> {
                currency = Currency.valueOf(param[1].trim());
                currencyModeService.setTargetCurrency(message.getChatId(), currency);
                editMessageReplyMarkup(message);
            }
            case RATE -> onUpdateReceived(getUpdateForCommand(CommandTitle.get_rate, message));
            case CONVERSION ->
                onUpdateReceived(getUpdateForCommand(CommandTitle.set_initial_target_currency, message));
            case CALCULATE -> sendMessage(message.getChatId(), currencyConversionService.getRate(
                    currencyModeService.getInitialCurrency(message.getChatId()),
                    currencyModeService.getTargetCurrency(message.getChatId())),
                message.getChat().getUserName());

            default -> log.error("TelegramBot: Something went wrong in handle callback query method!");
        }

    }
    private Update getUpdateForCommand(CommandTitle command, Message incomingMsg) {
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
        return update;
    }
    private void handleMessage(Message message) {
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> messageEntity = message.getEntities().stream()
                .filter(e -> "bot_command".equals(e.getType())).findFirst();
            if (messageEntity.isPresent()) {
                String command = message.getText().substring(1);
                CommandTitle title = CommandTitle.valueOf(command);
                switch (title) {
                    case start -> {
                        registerUser(message);
                        commandService.setCurrentCommand(CommandTitle.start);
                        sendMessage(message.getChatId(),
                            "Hello, " + message.getChat().getFirstName()
                                + ", welcome to the currency bot!" + EmojiParser.parseToUnicode(
                                ":wave:"), message.getChat().getUserName());
                        sendMessage(message.getChatId(), "What do you want to do?",
                            keyboardService.getInlineChoiceKeyBoard(message.getChatId()),
                            message.getChat().getUserName());
                    }
                    case help -> {
                        commandService.setCurrentCommand(CommandTitle.help);
                        sendMessage(message.getChatId(), HELP_TEXT,
                            message.getChat().getUserName());
                    }
                    case check_initial_target_currency -> {
                        commandService.setCurrentCommand(
                            CommandTitle.check_initial_target_currency);

                        sendMessage(message.getChatId(),
                            "Your initial currency is "
                                + currencyModeService.getInitialCurrency(
                                message.getChatId()) + " and target currency is "
                                + currencyModeService.getTargetCurrency(message.getChatId()),
                            message.getChat().getUserName());
                    }
                    case set_initial_target_currency -> {
                        commandService.setCurrentCommand(
                            CommandTitle.set_initial_target_currency);
                        sendMessage(message.getChatId(),
                            "Choose the initial and target currency:",
                            keyboardService.getInlineCurrencyKeyBoard(message.getChatId()),
                            message.getChat().getUserName());
                        commandService.setCurrentCommand(CommandTitle.set_amount);
                        sendMessage(message.getChatId(), "Enter the sum: ",
                            message.getChat().getUserName());
                    }
                    case set_amount -> {
                        commandService.setCurrentCommand(CommandTitle.set_amount);
                        sendMessage(message.getChatId(), "Enter the sum: ",
                            message.getChat().getUserName());
                    }
                    case get_rate -> {
                        commandService.setCurrentCommand(CommandTitle.get_rate);
                        sendMessage(message.getChatId(),
                            "What currency you are interested in?",
                            keyboardService.getInlineCurrencyKeyBoard(message.getChatId()),
                            message.getChat().getUserName());
                    }
                    default -> sendMessage(message.getChatId(),
                        "Sorry, command was not recognized. PLease, choose another one.",
                        message.getChat().getUserName());
                }
            }
        } else if (message.hasText()) {
            sendMessage(message.getChatId(), handleMessageDigit(message),
                message.getChat().getUserName());
        }
        log.info("TelegramBot: Handle message.");
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

    public void editMessageReplyMarkup(Message message) {
        try {
            execute(
                EditMessageReplyMarkup.builder()
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId())
                    .replyMarkup(keyboardService.getInlineCurrencyKeyBoard(message.getChatId()))
                    .build());
        } catch (TelegramApiException e) {
            log.error("TelegramBot: Handle Callback: " + e.getMessage());
        }
    }

    public void registerUser(Message message) {
        if(repository.findById(message.getChatId()).isEmpty()) {
            long chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setUsername(chat.getUserName());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setRegisterDate(new Timestamp(System.currentTimeMillis()));

            repository.save(user);
            log.info("Registered user with username: " + user);
        }
    }

    public void sendMessage(long chatId, String text, String username) {
        SendMessage sender = new SendMessage();
        sender.setChatId(chatId);
        sender.setText(text);
        try {
            execute(sender);
        } catch (TelegramApiException ex) {
            log.error("TelegramBot: Send message exception: " + ex.getMessage());
        }
        log.info("TelegramBot: Replied to user: " + username);
    }


    public void sendMessage(long chatId, String text, ReplyKeyboard keyboard, String username) {
        SendMessage sender = new SendMessage();
        sender.setChatId(chatId);
        sender.setText(text);
        sender.setReplyMarkup(keyboard);
        try {
            execute(sender);
        } catch (TelegramApiException ex) {
            log.error("TelegramBot: Send message exception: " + ex.getMessage());
        }
        log.info("TelegramBot: Replied to user: " + username);
    }
}
