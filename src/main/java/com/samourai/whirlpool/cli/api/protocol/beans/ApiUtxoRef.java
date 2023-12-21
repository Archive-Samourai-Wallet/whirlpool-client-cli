package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.whirlpool.protocol.beans.Utxo;
import javax.validation.constraints.NotEmpty;

public class ApiUtxoRef {
  @NotEmpty public String hash;
  @NotEmpty public int index;

  public ApiUtxoRef() {}

  public ApiUtxoRef(String hash, int index) {
    this.hash = hash;
    this.index = index;
  }

  public ApiUtxoRef(Utxo utxo) {
    this(utxo.getHash(), (int) utxo.getIndex());
  }
}
