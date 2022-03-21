package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import javax.validation.constraints.NotNull;

public class ApiReceiveRequest {
  @NotNull public WhirlpoolAccount account;
  public boolean increment;
  public String bipFormat;

  public ApiReceiveRequest() {}
}
