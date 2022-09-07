package com.spring.currency.bot.controller;

import com.spring.currency.bot.TelegramBot;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@Slf4j
@AllArgsConstructor
public class WebhookController {

  private final TelegramBot telegramBot;

  @PostMapping("/")
  public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
    log.info("WebhookController: controller updating by id:" + update.getUpdateId());
    return telegramBot.onWebhookUpdateReceived(update);
  }
//  @GetMapping("/")
//  public ResponseEntity<?> onUpdateReceived() {
//    return new ResponseEntity<>(HttpStatus.OK, HttpStatus.valueOf("OK"));
//  }
}
