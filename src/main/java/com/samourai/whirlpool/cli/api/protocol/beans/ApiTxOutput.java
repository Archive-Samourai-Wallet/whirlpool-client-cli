package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.wallet.api.backend.beans.WalletResponse;

public class ApiTxOutput {
  private int n;
  private long value;
  private String addr; // may be null
  private String pubkey; // may be null
  private String xpub; // may be null
  private String xpubPath; // may be null

  ApiTxOutput(WalletResponse.TxOutput txOutput) {
    this.n = txOutput.n;
    this.value = txOutput.value;
    this.addr = txOutput.addr;
    this.pubkey = txOutput.pubkey;
    if (txOutput.xpub != null) {
      this.xpub = txOutput.xpub.m;
      this.xpubPath = txOutput.xpub.path;
    }
  }

  public int getN() {
    return n;
  }

  public void setN(int n) {
    this.n = n;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public String getAddr() {
    return addr;
  }

  public void setAddr(String addr) {
    this.addr = addr;
  }

  public String getPubkey() {
    return pubkey;
  }

  public void setPubkey(String pubkey) {
    this.pubkey = pubkey;
  }

  public String getXpub() {
    return xpub;
  }

  public void setXpub(String xpub) {
    this.xpub = xpub;
  }

  public String getXpubPath() {
    return xpubPath;
  }

  public void setXpubPath(String xpubPath) {
    this.xpubPath = xpubPath;
  }
}
