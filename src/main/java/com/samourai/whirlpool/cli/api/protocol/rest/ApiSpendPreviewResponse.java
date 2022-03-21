package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.beans.SpendType;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoDetails;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import com.samourai.whirlpool.client.wallet.data.utxo.UtxoSupplier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiSpendPreviewResponse {
  private Collection<ApiUtxoDetails> spendFrom;
  private Map<String, Long> spendTo;
  private long spendValue;
  private long changeValue;
  private long minerFee;
  private int vSize;
  private int weight;
  private SpendType spendType;

  private ApiSpendPreviewResponse(
      Collection<ApiUtxoDetails> spendFrom,
      Map<String, Long> spendTo,
      long spendValue,
      long changeValue,
      long minerFee,
      int vSize,
      int weight,
      SpendType spendType) {
    this.spendFrom = spendFrom;
    this.spendTo = spendTo;
    this.spendValue = spendValue;
    this.changeValue = changeValue;
    this.minerFee = minerFee;
    this.vSize = vSize;
    this.weight = weight;
    this.spendType = spendType;
  }

  public ApiSpendPreviewResponse(SpendTx spendTx, UtxoSupplier utxoSupplier) {
    this(
        toUtxoRefs(spendTx.getSpendFrom(), utxoSupplier),
        spendTx.getSpendTo(),
        spendTx.getAmount(),
        spendTx.getChange(),
        spendTx.getFee(),
        spendTx.getvSize(),
        spendTx.getWeight(),
        spendTx.getSpendType());
  }

  private static Collection<ApiUtxoDetails> toUtxoRefs(
      List<MyTransactionOutPoint> outPoints, UtxoSupplier utxoSupplier) {
    return outPoints.stream()
        .map(
            outPoint -> {
              WhirlpoolUtxo whirlpoolUtxo =
                  utxoSupplier.findUtxo(outPoint.getTxHash().toString(), outPoint.getTxOutputN());
              return new ApiUtxoDetails(whirlpoolUtxo);
            })
        .collect(Collectors.toSet());
  }

  public Collection<ApiUtxoDetails> getSpendFrom() {
    return spendFrom;
  }

  public void setSpendFrom(List<ApiUtxoDetails> spendFrom) {
    this.spendFrom = spendFrom;
  }

  public Map<String, Long> getSpendTo() {
    return spendTo;
  }

  public void setSpendTo(Map<String, Long> spendTo) {
    this.spendTo = spendTo;
  }

  public long getSpendValue() {
    return spendValue;
  }

  public void setSpendValue(long spendValue) {
    this.spendValue = spendValue;
  }

  public long getChangeValue() {
    return changeValue;
  }

  public void setChangeValue(long changeValue) {
    this.changeValue = changeValue;
  }

  public long getMinerFee() {
    return minerFee;
  }

  public void setMinerFee(long minerFee) {
    this.minerFee = minerFee;
  }

  public int getvSize() {
    return vSize;
  }

  public int getWeight() {
    return weight;
  }

  public SpendType getSpendType() {
    return spendType;
  }
}
