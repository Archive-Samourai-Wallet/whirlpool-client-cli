package com.samourai.whirlpool.cli.api.protocol.rest;

import javax.validation.constraints.NotEmpty;

public class ApiCliRestoreBackupRequest extends AbstractApiCliInitRequest {
  @NotEmpty public String passphrase;
  @NotEmpty public String backup;

  public ApiCliRestoreBackupRequest() {}
}
