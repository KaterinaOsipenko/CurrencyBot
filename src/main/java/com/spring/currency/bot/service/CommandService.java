package com.spring.currency.bot.service;


import com.spring.currency.bot.model.CommandTitle;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

public interface CommandService {

   List<BotCommand> getListOfBotCommands();
   String getCurrentCommand();

   void setCurrentCommand(CommandTitle command);
}
