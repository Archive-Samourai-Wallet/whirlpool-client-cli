package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiCliOpenWalletRequest {
  @NotEmpty public String seedPassphrase;

  public ApiCliOpenWalletRequest() {}
}
