package com.samourai.whirlpool.cli.beans;

import java.util.Optional;

public enum BusyReason {
  FETCHING_WALLET;

  public static Optional<BusyReason> find(String value) {
    try {
      return Optional.of(valueOf(value));
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
