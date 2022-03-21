package com.samourai.whirlpool.cli.api.protocol.rest;

public class ApiCliCreateResponse extends ApiCliInitResponse {
  private String mnemonic;
  private String passphrase;

  public ApiCliCreateResponse(String apiKey, String mnemonic, String passphrase) {
    super(apiKey);
    this.mnemonic = mnemonic;
    this.passphrase = passphrase;
  }

  public String getMnemonic() {
    return mnemonic;
  }

  public String getPassphrase() {
    return passphrase;
  }
}
