package com.samourai.whirlpool.cli.persistence.beans;

import java.util.Optional;

public enum LogType {
  TX0,
  MIX_SUCCESS,
  MIX_FAIL,
  RECEIVED;

  public static Optional<LogType> find(String value) {
    try {
      return Optional.of(valueOf(value));
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
