package com.samourai.whirlpool.cli.api.controllers.rest;

public class RestErrorResponse {
  public int errorCode;
  public String message;

  public RestErrorResponse() {}

  public RestErrorResponse(int errorCode, String message) {
    this.errorCode = errorCode;
    this.message = message;
  }

  @Override
  public String toString() {
    return "RestErrorResponse{" + "errorCode=" + errorCode + ", message='" + message + '\'' + '}';
  }
}
