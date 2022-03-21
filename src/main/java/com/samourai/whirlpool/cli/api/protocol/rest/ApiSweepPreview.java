package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.send.beans.SweepPreview;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoRef;
import java.util.Collection;
import java.util.stream.Collectors;

public class ApiSweepPreview {
  private long amount;
  private String address;
  private String bipFormat;
  private long fee;
  private Collection<ApiUtxoRef> utxos;

  public ApiSweepPreview(SweepPreview sweepPreview) {
    this.amount = sweepPreview.getAmount();
    this.address = sweepPreview.getAddress();
    this.bipFormat = sweepPreview.getBipFormat().getId();
    this.fee = sweepPreview.getFee();
    this.utxos =
        sweepPreview.getUtxos().stream()
            .map(utxo -> new ApiUtxoRef(utxo.tx_hash, utxo.tx_output_n))
            .collect(Collectors.toList());
  }

  public long getAmount() {
    return amount;
  }

  public String getAddress() {
    return address;
  }

  public String getBipFormat() {
    return bipFormat;
  }

  public long getFee() {
    return fee;
  }

  public Collection<ApiUtxoRef> getUtxos() {
    return utxos;
  }
}
