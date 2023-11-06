package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.cli.api.protocol.beans.ApiCliStateExternalDestination;
import com.samourai.whirlpool.cli.beans.CliState;
import com.samourai.whirlpool.cli.beans.CliStatus;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.client.wallet.beans.ExternalDestination;

public class ApiCliStateResponse {
  private CliStatus cliStatus;
  private String cliMessage;
  private boolean loggedIn;
  private Integer torProgress;

  private String network;
  private String serverUrl;
  private String serverName;
  private String dojoUrl;
  private boolean tor;
  private boolean dojo;
  private String version;
  private ApiCliStateExternalDestination externalDestination;

  public ApiCliStateResponse(
      CliState cliState, CliConfig cliConfig, ExternalDestination externalDestination) {
    this.cliStatus = cliState.getCliStatus();
    this.cliMessage = cliState.getCliMessage();
    this.loggedIn = cliState.isLoggedIn();
    this.torProgress = cliState.getTorProgress();
    this.network = cliConfig.getServer().getWhirlpoolNetwork().getParams().getPaymentProtocolId();
    this.serverUrl = cliConfig.computeServerUrl();
    this.serverName = cliConfig.getServer().name();
    this.dojoUrl = cliConfig.getDojo().getUrl();
    this.tor = cliConfig.getTor();
    this.dojo = cliConfig.isDojoEnabled();
    this.version = cliConfig.getBuildVersion();
    this.externalDestination =
        externalDestination != null
            ? new ApiCliStateExternalDestination(externalDestination)
            : null;
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

  public Integer getTorProgress() {
    return torProgress;
  }

  public String getNetwork() {
    return network;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public String getServerName() {
    return serverName;
  }

  public String getDojoUrl() {
    return dojoUrl;
  }

  public boolean isTor() {
    return tor;
  }

  public boolean isDojo() {
    return dojo;
  }

  public String getVersion() {
    return version;
  }

  public ApiCliStateExternalDestination getExternalDestination() {
    return externalDestination;
  }
}
