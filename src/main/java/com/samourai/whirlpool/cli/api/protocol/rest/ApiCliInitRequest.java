package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiCliInitRequest extends AbstractApiCliInitRequest {
  @NotEmpty public String pairingPayload;

  public ApiCliInitRequest() {}
}
