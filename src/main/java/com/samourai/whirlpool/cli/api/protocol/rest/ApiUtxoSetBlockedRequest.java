package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoRef;
import javax.validation.constraints.NotNull;

public class ApiUtxoSetBlockedRequest {
  @NotNull public ApiUtxoRef utxo;
  public boolean blocked;

  public ApiUtxoSetBlockedRequest() {}
}
