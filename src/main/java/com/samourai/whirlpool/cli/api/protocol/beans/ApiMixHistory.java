package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.whirlpool.client.wallet.beans.MixHistory;

public class ApiMixHistory {
  private long startupTime;
  private int mixedCount;
  private int failedCount;
  private Long mixedLastTime;
  private Long failedLastTime;
  private long mixedVolume;
  private int externalXpubCount;
  private long externalXpubVolume;
  private Long externalXpubLastTime;

  public ApiMixHistory(MixHistory mixHistory) {
    this.startupTime = mixHistory.getStartupTime();
    this.mixedCount = mixHistory.getMixedCount();
    this.failedCount = mixHistory.getFailedCount();
    this.mixedLastTime = mixHistory.getMixedLastTime();
    this.failedLastTime = mixHistory.getFailedLastTime();
    this.mixedVolume = mixHistory.getMixedVolume();
    this.externalXpubCount = mixHistory.getExternalXpubCount();
    this.externalXpubVolume = mixHistory.getExternalXpubVolume();
    this.externalXpubLastTime = mixHistory.getExternalXpubLastTime();
  }

  public long getStartupTime() {
    return startupTime;
  }

  public int getMixedCount() {
    return mixedCount;
  }

  public int getFailedCount() {
    return failedCount;
  }

  public Long getMixedLastTime() {
    return mixedLastTime;
  }

  public Long getFailedLastTime() {
    return failedLastTime;
  }

  public long getMixedVolume() {
    return mixedVolume;
  }

  public int getExternalXpubCount() {
    return externalXpubCount;
  }

  public long getExternalXpubVolume() {
    return externalXpubVolume;
  }

  public Long getExternalXpubLastTime() {
    return externalXpubLastTime;
  }
}
