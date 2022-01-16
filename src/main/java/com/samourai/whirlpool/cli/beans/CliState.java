package com.samourai.whirlpool.cli.beans;

import java.util.Collection;

public class CliState {
  private CliStatus cliStatus;
  private String cliMessage;
  private boolean loggedIn;
  private boolean fetchingWallet;
  private Integer torProgress;

  public CliState(
      CliStatus cliStatus,
      String cliMessage,
      boolean loggedIn,
      Collection<BusyReason> busyReason,
      Integer torProgress) {
    this.cliStatus = cliStatus;
    this.cliMessage = cliMessage;
    this.loggedIn = loggedIn;
    this.fetchingWallet = busyReason.contains(BusyReason.FETCHING_WALLET);
    this.torProgress = torProgress;
  }

  public CliStatus getCliStatus() {
    return cliStatus;
  }

  public String getCliMessage() {
    return cliMessage;
  }

  public boolean isLoggedIn() {
    return loggedIn;
  }

  public boolean isFetchingWallet() {
    return fetchingWallet;
  }

  public Integer getTorProgress() {
    return torProgress;
  }
}
