package com.spring.currency.bot.service;


import com.spring.currency.bot.model.CommandTitle;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

@Service
@Slf4j
public class CommandServiceImpl implements CommandService {

  private  final List<BotCommand> commandList = new ArrayList<>();

  private BotCommand currentCommand;

  public CommandServiceImpl () {
    log.info("CommandService created!");
  }

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
    commandList.add(new BotCommand(CommandTitle.start.label, "get a welcome message"));
    commandList.add(new BotCommand(CommandTitle.set_initial_target_currency.label, "set initial and target currency"));
    commandList.add(new BotCommand(CommandTitle.help.label, "more info"));
    commandList.add(new BotCommand(CommandTitle.set_amount.label, "set the amount you want to transfer"));
    commandList.add(new BotCommand(CommandTitle.check_initial_target_currency.label, "check your currency"));
    commandList.add(new BotCommand(CommandTitle.get_rate.label, "get actual rate"));
    log.info("CommandServiceImpl: Creation bot commands.");
    return commandList;
  }
}
