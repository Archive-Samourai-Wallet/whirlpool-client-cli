package com.samourai.whirlpool.cli.api.protocol.beans;

public class ApiUtxoRef {
  private String hash;
  private int index;

  public ApiUtxoRef(String hash, int index) {
    this.hash = hash;
    this.index = index;
  }

  public String getHash() {
    return hash;
  }

  public int getIndex() {
    return index;
  }
}
