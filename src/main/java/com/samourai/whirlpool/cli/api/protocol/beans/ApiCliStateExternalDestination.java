package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.wallet.beans.ExternalDestination;

public class ApiCliStateExternalDestination {
  private String xpub;
  private int mixs;

  public ApiCliStateExternalDestination(ExternalDestination ed) {
    this.xpub = ClientUtils.maskString(ed.getXpub());
    this.mixs = ed.getMixs();
  }

  public String getXpub() {
    return xpub;
  }

  public int getMixs() {
    return mixs;
  }
}
