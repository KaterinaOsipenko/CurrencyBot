package com.spring.currency.bot.handler;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageHandler {

  BotApiMethod<?> handleMessage(Message message);

}
