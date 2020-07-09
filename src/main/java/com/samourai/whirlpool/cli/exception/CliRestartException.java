package com.samourai.whirlpool.cli.exception;

import com.samourai.whirlpool.client.exception.NotifiableException;

public class CliRestartException extends NotifiableException {

  public CliRestartException(String error) {
    super(error);
  }
}
