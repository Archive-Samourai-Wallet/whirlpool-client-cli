package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiCliCreateRequest extends AbstractApiCliInitRequest {
  @NotEmpty public String passphrase;
  public boolean testnet;
  @NotEmpty public String dojoUrl;
  @NotEmpty public String dojoApiKey;

  public ApiCliCreateRequest() {}
}
