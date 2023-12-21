package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.whirlpool.cli.config.CliConfigFile;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.wallet.beans.ExternalDestination;

public class ApiCliStateExternalDestination {
  private String xpub; // only set when enabled
  private int mixs;
  private boolean enabled;

  public ApiCliStateExternalDestination(
      ExternalDestination externalDestination, CliConfigFile.ExternalDestinationConfig edConfig) {
    this.xpub =
        externalDestination != null ? ClientUtils.maskString(externalDestination.getXpub()) : null;
    this.mixs = edConfig.getMixs();
    this.enabled = !edConfig.isDisabled();
  }

  public String getXpub() {
    return xpub;
  }

  public int getMixs() {
    return mixs;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
