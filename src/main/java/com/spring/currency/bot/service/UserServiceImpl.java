package com.spring.currency.bot.service;

import com.spring.currency.bot.dao.UserRepository;
import com.spring.currency.bot.model.User;
import java.sql.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

  @Autowired
  private UserRepository repository;

  @Override
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
}
