package com.samourai.whirlpool.cli.api.protocol.rest;

public class ApiAddressPrivateResponse {
  private String privateKey;
  private String redeemScript;

  public ApiAddressPrivateResponse(String privateKey, String redeemScript) {
    this.privateKey = privateKey;
    this.redeemScript = redeemScript;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public String getRedeemScript() {
    return redeemScript;
  }
}
