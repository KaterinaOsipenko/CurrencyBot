package com.spring.currency.bot.service;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface UserService {

  void registerUser(Message message);

}
