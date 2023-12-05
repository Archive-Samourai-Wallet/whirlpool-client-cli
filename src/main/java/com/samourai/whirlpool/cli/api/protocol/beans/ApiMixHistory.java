package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.whirlpool.client.wallet.beans.MixHistory;

public class ApiMixHistory {
  private long startupTime;
  private int nbMixed;
  private int nbFailed;
  private long mixedVolume;

  public ApiMixHistory(MixHistory mixHistory) {
    this.startupTime = mixHistory.getStartupTime();
    this.nbMixed = mixHistory.getNbMixed();
    this.nbFailed = mixHistory.getNbFailed();
    this.mixedVolume = mixHistory.getMixedVolume();
  }

  public long getStartupTime() {
    return startupTime;
  }

  public int getNbMixed() {
    return nbMixed;
  }

  public int getNbFailed() {
    return nbFailed;
  }

  public long getMixedVolume() {
    return mixedVolume;
  }
}
