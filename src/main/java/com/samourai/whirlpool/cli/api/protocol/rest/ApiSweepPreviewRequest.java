package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.api.backend.MinerFeeTarget;
import javax.validation.constraints.NotEmpty;

public class ApiSweepPreviewRequest {
  @NotEmpty public String privateKey;
  public MinerFeeTarget minerFeeTarget = MinerFeeTarget.BLOCKS_4;

  public ApiSweepPreviewRequest() {}
}
