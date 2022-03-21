package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiTx0Request extends ApiTx0PreviewRequest {
  @NotEmpty public String poolId;

  public ApiTx0Request() {}
}
