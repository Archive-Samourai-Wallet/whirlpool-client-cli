package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiPaynymFollowRequest {
  @NotEmpty public String paymentCodeTarget;

  public ApiPaynymFollowRequest() {}
}
