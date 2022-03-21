package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiAddressPrivateRequest {
  @NotEmpty public String address;

  public ApiAddressPrivateRequest() {}
}
