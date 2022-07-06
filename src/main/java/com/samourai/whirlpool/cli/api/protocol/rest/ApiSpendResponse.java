package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.ricochet.Ricochet;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.whirlpool.client.wallet.data.utxo.UtxoSupplier;

public class ApiSpendResponse extends ApiSpendPreviewResponse {
  private String txid;

  public ApiSpendResponse(String txid, SpendTx spendTx, UtxoSupplier utxoSupplier) {
    super(spendTx, utxoSupplier);
    this.txid = txid;
  }

  public ApiSpendResponse(String txid, Ricochet ricochet, UtxoSupplier utxoSupplier) {
    super(ricochet, utxoSupplier);
    this.txid = txid;
  }

  public String getTxid() {
    return txid;
  }

  public void setTxid(String txid) {
    this.txid = txid;
  }
}
