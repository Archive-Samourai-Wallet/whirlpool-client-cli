package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiPaynymUnfollowRequest {
  @NotEmpty public String paymentCodeTarget;

  public ApiPaynymUnfollowRequest() {}
}
