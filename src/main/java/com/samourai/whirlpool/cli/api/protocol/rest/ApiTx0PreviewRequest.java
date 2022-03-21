package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoRef;
import com.samourai.whirlpool.client.wallet.beans.Tx0FeeTarget;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ApiTx0PreviewRequest {
  @NotEmpty public ApiUtxoRef[] inputs;
  @NotNull public Tx0FeeTarget tx0FeeTarget;
  @NotNull public Tx0FeeTarget mixFeeTarget;

  public ApiTx0PreviewRequest() {}
}
