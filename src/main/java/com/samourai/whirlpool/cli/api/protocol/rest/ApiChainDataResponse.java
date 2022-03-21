package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.api.backend.beans.WalletResponse;

public class ApiChainDataResponse {
  private int blockHeight;
  private String blockHash;
  private long blockTime;

  public ApiChainDataResponse(WalletResponse.InfoBlock block) throws Exception {
    this.blockHeight = block.height;
    this.blockHash = block.hash;
    this.blockTime = block.time;
  }

  public int getBlockHeight() {
    return blockHeight;
  }

  public String getBlockHash() {
    return blockHash;
  }

  public long getBlockTime() {
    return blockTime;
  }
}
