package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.whirlpool.client.whirlpool.beans.Pool;
import com.samourai.whirlpool.protocol.websocket.notifications.MixStatus;

public class ApiPool {
  private String poolId;
  private long denomination;
  private long feeValue;
  private long mustMixBalanceMin;
  private long mustMixBalanceCap;
  private long mustMixBalanceMax;
  private int minAnonymitySet;
  private int tx0MaxOutputs;
  private int nbRegistered;
  private int mixAnonymitySet;
  private MixStatus mixStatus;
  private long elapsedTime;
  private int nbConfirmed;
  private long tx0BalanceMin;

  public ApiPool() {}

  public ApiPool(Pool pool, long tx0BalanceMin) {
    this.poolId = pool.getPoolId();
    this.denomination = pool.getDenomination();
    this.feeValue = pool.getFeeValue();
    this.mustMixBalanceMin = pool.getMustMixBalanceMin();
    this.mustMixBalanceCap = pool.getMustMixBalanceCap();
    this.mustMixBalanceMax = pool.getMustMixBalanceMax();
    this.minAnonymitySet = pool.getMinAnonymitySet();
    this.tx0MaxOutputs = pool.getTx0MaxOutputs();
    this.nbRegistered = pool.getNbRegistered();
    this.mixAnonymitySet = pool.getMixAnonymitySet();
    this.mixStatus = pool.getMixStatus();
    this.elapsedTime = pool.getElapsedTime();
    this.nbConfirmed = pool.getNbConfirmed();
    this.tx0BalanceMin = tx0BalanceMin;
  }

  public String getPoolId() {
    return poolId;
  }

  public long getDenomination() {
    return denomination;
  }

  public long getFeeValue() {
    return feeValue;
  }

  public long getMustMixBalanceMin() {
    return mustMixBalanceMin;
  }

  public long getMustMixBalanceCap() {
    return mustMixBalanceCap;
  }

  public long getMustMixBalanceMax() {
    return mustMixBalanceMax;
  }

  public int getMinAnonymitySet() {
    return minAnonymitySet;
  }

  public int getTx0MaxOutputs() {
    return tx0MaxOutputs;
  }

  public int getNbRegistered() {
    return nbRegistered;
  }

  public int getMixAnonymitySet() {
    return mixAnonymitySet;
  }

  public MixStatus getMixStatus() {
    return mixStatus;
  }

  public long getElapsedTime() {
    return elapsedTime;
  }

  public int getNbConfirmed() {
    return nbConfirmed;
  }

  public long getTx0BalanceMin() {
    return tx0BalanceMin;
  }
}
