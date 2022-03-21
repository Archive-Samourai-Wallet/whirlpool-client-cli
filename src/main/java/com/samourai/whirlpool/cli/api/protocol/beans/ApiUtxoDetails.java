package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;

public class ApiUtxoDetails {
  private String hash;
  private int index;
  private long value;
  private Integer blockHeight; // null when unconfirmed
  private String path;
  private String address;
  private String bipFormat;
  private WhirlpoolAccount account;

  public ApiUtxoDetails(WhirlpoolUtxo whirlpoolUtxo) {
    UnspentOutput utxo = whirlpoolUtxo.getUtxo();
    this.hash = utxo.tx_hash;
    this.index = utxo.tx_output_n;
    this.value = utxo.value;
    this.blockHeight = whirlpoolUtxo.getBlockHeight();
    this.path = whirlpoolUtxo.getPathAddress();
    this.address = utxo.addr;
    this.bipFormat = whirlpoolUtxo.getBipWallet().getBipFormat().getId();
    this.account = whirlpoolUtxo.getAccount();
  }

  public String getHash() {
    return hash;
  }

  public int getIndex() {
    return index;
  }

  public long getValue() {
    return value;
  }

  public Integer getBlockHeight() {
    return blockHeight;
  }

  public String getPath() {
    return path;
  }

  public String getAddress() {
    return address;
  }

  public String getBipFormat() {
    return bipFormat;
  }

  public WhirlpoolAccount getAccount() {
    return account;
  }
}
