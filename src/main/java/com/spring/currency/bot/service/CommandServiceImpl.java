package com.spring.currency.bot.service;


import com.spring.currency.bot.model.CommandTitle;
import com.spring.currency.bot.utils.CommandDescription;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

@Service
@Slf4j
public class CommandServiceImpl implements CommandService {

  private final List<BotCommand> commandList = new ArrayList<>();

  private BotCommand currentCommand;

  public String getCurrentCommand() {
    return currentCommand.getCommand();
  }

  public void setCurrentCommand(CommandTitle currentCommand) {
    for (BotCommand botCommand : commandList) {
      if (botCommand.getCommand().equals(currentCommand.label)) {
        this.currentCommand = botCommand;
      }
    }
  }

  @Override
  public List<BotCommand> getListOfBotCommands() {
    log.info("CommandServiceImpl: Creation bot commands.");
    commandList.add(new BotCommand(CommandTitle.start.label, CommandDescription.startDesc));
    commandList.add(new BotCommand(CommandTitle.set_initial_target_currency.label, CommandDescription.setCurrencyDesc));
    commandList.add(new BotCommand(CommandTitle.help.label, CommandDescription.helpDesc));
    commandList.add(new BotCommand(CommandTitle.set_amount.label, CommandDescription.setAmountDesc));
    commandList.add(new BotCommand(CommandTitle.check_initial_target_currency.label, CommandDescription.checkCurrencyDesc));
    commandList.add(new BotCommand(CommandTitle.get_rate.label, CommandDescription.getRateDesc));
    return commandList;
  }
}
