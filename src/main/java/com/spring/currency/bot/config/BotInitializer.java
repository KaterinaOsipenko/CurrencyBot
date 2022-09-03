package com.spring.currency.bot.config;

import com.spring.currency.bot.TelegramBot;
import com.spring.currency.bot.service.CommandService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Slf4j
@Configuration
@AllArgsConstructor
public class BotInitializer {

    private final BotConfig botConfig;

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(botConfig.getPath()).build();
    }

    @Bean
    public TelegramBot init(SetWebhook setWebhook, CommandService commandService) {
        log.info("Bot init.");
        return new TelegramBot(botConfig, commandService, setWebhook);
    }
}
