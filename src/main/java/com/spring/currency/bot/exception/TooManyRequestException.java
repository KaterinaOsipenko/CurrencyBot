package com.spring.currency.bot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class TooManyRequestException extends HttpClientErrorException {

  public TooManyRequestException(HttpStatus statusCode) {
    super(statusCode);
  }
}
