package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiAuthLoginRequest {
  @NotEmpty public String apiKey;

  public ApiAuthLoginRequest() {}
}
