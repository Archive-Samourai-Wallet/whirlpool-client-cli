package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.wallet.api.backend.beans.WalletResponse;

public class ApiTxOut {
  private String txid;
  private int vout;
  private long value;
  private String xpub; // may be null
  private String xpubPath; // may be null
  private String addr; // may be null
  private String pubkey; // may be null

  ApiTxOut(WalletResponse.TxOut txOut) {
    this.txid = txOut.txid;
    this.vout = txOut.vout;
    this.value = txOut.value;
    if (txOut.xpub != null) {
      this.xpub = txOut.xpub.m;
      this.xpubPath = txOut.xpub.path;
    }
    this.addr = txOut.addr;
    this.pubkey = txOut.pubkey;
  }

  public String getTxid() {
    return txid;
  }

  public void setTxid(String txid) {
    this.txid = txid;
  }

  public int getVout() {
    return vout;
  }

  public void setVout(int vout) {
    this.vout = vout;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
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
}
