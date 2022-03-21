package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiAddressSignRequest {
  @NotEmpty public String address;
  @NotEmpty public String message;

  public ApiAddressSignRequest() {}
}
