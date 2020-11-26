package com.samourai.whirlpool.cli.api.protocol.beans;

import javax.validation.constraints.NotEmpty;

public class ApiUtxoRef {
  @NotEmpty public String hash;
  @NotEmpty public int index;

  public ApiUtxoRef() {}
}
