package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.api.backend.MinerFee;
import java.util.Map;

public class ApiMinerFeeResponse {
  private Map<String, Integer> minerFee;

  public ApiMinerFeeResponse(MinerFee minerFee) throws Exception {
    this.minerFee = minerFee._getMap();
  }

  public Map<String, Integer> getMinerFee() {
    return minerFee;
  }
}
