package com.samourai.whirlpool.cli.api.protocol.rest;

public class ApiAddressSignResponse {
  private String signature;

  public ApiAddressSignResponse(String signature) {
    this.signature = signature;
  }

  public String getSignature() {
    return signature;
  }
}
