package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.cli.api.protocol.beans.ApiCliConfig;
import javax.validation.constraints.NotNull;

public class ApiCliConfigRequest {
  @NotNull public ApiCliConfig config;

  public ApiCliConfigRequest() {}
}
