package com.spring.currency.bot.model;

public enum CommandTitle {

  help("/help"),
  start("/start"),
  set_initial_target_currency("/set_initial_target_currency"),
  set_amount("/set_amount"),
  check_initial_target_currency("/check_initial_target_currency"),
  get_rate("/get_rate");

  public final String label;
  CommandTitle(String label) {
    this.label = label;
  }
}
