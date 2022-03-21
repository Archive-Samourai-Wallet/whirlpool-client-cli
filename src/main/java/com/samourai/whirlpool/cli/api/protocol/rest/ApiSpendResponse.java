package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.whirlpool.client.wallet.data.utxo.UtxoSupplier;

public class ApiSpendResponse extends ApiSpendPreviewResponse {
  private String txid;

  public ApiSpendResponse(SpendTx spendTx, UtxoSupplier utxoSupplier, String txid) {
    super(spendTx, utxoSupplier);
    this.txid = txid;
  }

  public String getTxid() {
    return txid;
  }

  public void setTxid(String txid) {
    this.txid = txid;
  }
}
