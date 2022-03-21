package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.wallet.api.backend.beans.WalletResponse;

public class ApiTxInput {
  private int vin;
  private ApiTxOut prevOut;
  private long sequence;

  public ApiTxInput(WalletResponse.TxInput txInput) {
    this.vin = txInput.vin;
    if (txInput.prev_out != null) {
      this.prevOut = new ApiTxOut(txInput.prev_out);
    }
    this.sequence = txInput.sequence;
  }

  public int getVin() {
    return vin;
  }

  public void setVin(int vin) {
    this.vin = vin;
  }

  public ApiTxOut getPrevOut() {
    return prevOut;
  }

  public void setPrevOut(ApiTxOut prevOut) {
    this.prevOut = prevOut;
  }

  public long getSequence() {
    return sequence;
  }

  public void setSequence(long sequence) {
    this.sequence = sequence;
  }
}
