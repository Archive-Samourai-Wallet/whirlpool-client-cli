package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiAuthRefreshRequest {
  @NotEmpty public String refreshToken;

  public ApiAuthRefreshRequest() {}
}
