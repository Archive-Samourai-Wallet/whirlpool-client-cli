package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.ricochet.Ricochet;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.beans.SpendType;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoDetails;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import com.samourai.whirlpool.client.wallet.data.utxo.UtxoSupplier;
import java.util.*;
import java.util.stream.Collectors;
import org.bitcoinj.core.TransactionOutPoint;

public class ApiSpendPreviewResponse {
  private Collection<ApiUtxoDetails> spendFrom;
  private Map<String, Long> spendTo;
  private long spendValue;
  private long changeValue;
  private long minerFee;
  private long samouraiFee;
  private int vSize;
  private int weight;
  private SpendType spendType;

  protected ApiSpendPreviewResponse(
      Collection<ApiUtxoDetails> spendFrom,
      Map<String, Long> spendTo,
      long spendValue,
      long changeValue,
      long minerFee,
      long samouraiFee,
      int vSize,
      int weight,
      SpendType spendType) {
    this.spendFrom = spendFrom;
    this.spendTo = spendTo;
    this.spendValue = spendValue;
    this.changeValue = changeValue;
    this.minerFee = minerFee;
    this.samouraiFee = samouraiFee;
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
        0,
        spendTx.getTx().getVirtualTransactionSize(),
        spendTx.getTx().getWeight(),
        spendTx.getSpendType());
  }

  public ApiSpendPreviewResponse(Ricochet ricochet, UtxoSupplier utxoSupplier) {
    this(
        toUtxoRefs(ricochet.getSpendFrom(), utxoSupplier),
        computeSpendTo(ricochet.getDestination(), ricochet.getSpend_amount()),
        ricochet.getSpend_amount(),
        ricochet.getChange_amount(),
        ricochet.getTotal_miner_fee(),
        ricochet.getSamourai_fee(),
        (int) ricochet.getTotal_vSize(),
        (int) ricochet.getTotal_weight(),
        SpendType.RICOCHET);
  }

  public static Map<String, Long> computeSpendTo(String destination, long amount) {
    Map<String, Long> map = new LinkedHashMap<>();
    map.put(destination, amount);
    return map;
  }

  private static Collection<ApiUtxoDetails> toUtxoRefs(
      Collection<? extends TransactionOutPoint> outPoints, UtxoSupplier utxoSupplier) {
    return outPoints.stream()
        .map(
            outPoint -> {
              WhirlpoolUtxo whirlpoolUtxo =
                  utxoSupplier.findUtxo(outPoint.getHash().toString(), (int) outPoint.getIndex());
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

  public long getSamouraiFee() {
    return samouraiFee;
  }

  public void setSamouraiFee(long samouraiFee) {
    this.samouraiFee = samouraiFee;
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
