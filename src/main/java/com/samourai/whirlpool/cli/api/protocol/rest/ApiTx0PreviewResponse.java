package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.client.tx0.Tx0Preview;

public class ApiTx0PreviewResponse {
  private long tx0MinerFee;
  private long mixMinerFee;
  private long premixMinerFee;
  private int tx0MinerFeePrice;
  private int mixMinerFeePrice;
  private long feeValue;
  private long feeChange;
  private long premixValue;
  private long changeValue;
  private int nbPremix;
  private int feeDiscountPercent;

  public ApiTx0PreviewResponse(Tx0Preview tx0Preview) {
    this.tx0MinerFee = tx0Preview.getTx0MinerFee();
    this.mixMinerFee = tx0Preview.getMixMinerFee();
    this.premixMinerFee = tx0Preview.getPremixMinerFee();
    this.tx0MinerFeePrice = tx0Preview.getTx0MinerFeePrice();
    this.mixMinerFeePrice = tx0Preview.getMixMinerFeePrice();
    this.feeValue = tx0Preview.getFeeValue();
    this.feeChange = tx0Preview.getFeeChange();
    this.premixValue = tx0Preview.getPremixValue();
    this.changeValue = tx0Preview.getChangeValue();
    this.nbPremix = tx0Preview.getNbPremix();
    this.feeDiscountPercent = tx0Preview.getFeeDiscountPercent();
  }

  public long getTx0MinerFee() {
    return tx0MinerFee;
  }

  public long getMixMinerFee() {
    return mixMinerFee;
  }

  public long getPremixMinerFee() {
    return premixMinerFee;
  }

  public int getTx0MinerFeePrice() {
    return tx0MinerFeePrice;
  }

  public int getMixMinerFeePrice() {
    return mixMinerFeePrice;
  }

  public long getFeeValue() {
    return feeValue;
  }

  public long getFeeChange() {
    return feeChange;
  }

  public long getPremixValue() {
    return premixValue;
  }

  public long getChangeValue() {
    return changeValue;
  }

  public int getNbPremix() {
    return nbPremix;
  }

  public int getFeeDiscountPercent() {
    return feeDiscountPercent;
  }
}
