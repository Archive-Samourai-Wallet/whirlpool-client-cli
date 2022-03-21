package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.hd.BipAddress;

public class ApiReceiveAddress {
  private String bipFormat;
  private String address;
  private String path;

  public ApiReceiveAddress(BipFormat bipFormat, BipAddress bipAddress) {
    this.bipFormat = bipFormat.getId();
    this.address = bipAddress.getAddressString();
    this.path = bipAddress.getPathAddress();
  }

  public String getBipFormat() {
    return bipFormat;
  }

  public String getAddress() {
    return address;
  }

  public String getPath() {
    return path;
  }
}
