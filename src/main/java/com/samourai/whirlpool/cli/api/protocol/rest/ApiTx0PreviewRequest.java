package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoRef;
import com.samourai.whirlpool.client.wallet.beans.Tx0FeeTarget;
import javax.validation.constraints.NotNull;

public class ApiTx0PreviewRequest {
  /*@NotNull TODO next release*/ public ApiUtxoRef[] inputs;
  @NotNull public Tx0FeeTarget feeTarget;
  @NotNull public String poolId;

  public ApiTx0PreviewRequest() {}
}
