package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiCliRestoreExternalRequest extends AbstractApiCliInitRequest {
  @NotEmpty public String passphrase;
  public boolean appendPassphrase;
  @NotEmpty public String mnemonic;
  public boolean testnet;
  @NotEmpty public String dojoUrl;
  @NotEmpty public String dojoApiKey;

  public ApiCliRestoreExternalRequest() {}
}
