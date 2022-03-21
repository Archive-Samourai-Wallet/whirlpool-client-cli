package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiReceiveAddress;

public class ApiReceiveResponse extends ApiReceiveAddress {

  public ApiReceiveResponse(BipFormat bipFormat, BipAddress bipAddress) {
    super(bipFormat, bipAddress);
  }
}
