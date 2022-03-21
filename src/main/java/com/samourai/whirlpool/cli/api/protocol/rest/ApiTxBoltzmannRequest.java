package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiTxBoltzmannRequest {
  @NotEmpty public String txid;

  public ApiTxBoltzmannRequest() {}
}
