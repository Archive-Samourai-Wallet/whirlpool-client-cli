package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.wallet.api.backend.beans.WalletResponse;

public class ApiChainBlock {
  private int height;
  private String hash;
  private long time;

  public ApiChainBlock() {}

  public ApiChainBlock(WalletResponse.InfoBlock infoBlock) {
    this.height = infoBlock.height;
    this.hash = infoBlock.hash;
    this.time = infoBlock.time * 1000;
  }

  public int getHeight() {
    return height;
  }

  public String getHash() {
    return hash;
  }

  public long getTime() {
    return time;
  }
}
