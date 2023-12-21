package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.whirlpool.client.mix.handler.DestinationType;
import com.samourai.whirlpool.client.mix.listener.MixFailReason;
import com.samourai.whirlpool.client.wallet.beans.MixResult;

public class ApiMixResult {
  private long time;
  private boolean success;
  private String poolId;
  private long amount;
  private boolean liquidity;
  private ApiUtxoRef destinationUtxo;
  private String destinationAddress;
  private DestinationType destinationType;
  private String destinationPath;
  private ApiUtxoRef failUtxo;
  private MixFailReason failReason;
  private String failError;

  public ApiMixResult(MixResult mixResult) {
    this.time = mixResult.getTime();
    this.success = mixResult.isSuccess();
    this.poolId = mixResult.getPoolId();
    this.amount = mixResult.getAmount();
    this.liquidity = mixResult.isLiquidity();
    this.destinationUtxo =
        mixResult.getDestinationUtxo() != null
            ? new ApiUtxoRef(mixResult.getDestinationUtxo())
            : null;
    this.destinationAddress =
        mixResult.getDestinationAddress() != null ? mixResult.getDestinationAddress() : null;
    this.destinationType =
        mixResult.getDestinationType() != null ? mixResult.getDestinationType() : null;
    this.destinationPath =
        mixResult.getDestinationPath() != null ? mixResult.getDestinationPath() : null;
    this.failUtxo =
        mixResult.getFailUtxo() != null ? new ApiUtxoRef(mixResult.getFailUtxo()) : null;
    this.failReason = mixResult.getFailReason();
    this.failError = mixResult.getFailError();
  }

  public long getTime() {
    return time;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getPoolId() {
    return poolId;
  }

  public long getAmount() {
    return amount;
  }

  public boolean isLiquidity() {
    return liquidity;
  }

  public ApiUtxoRef getDestinationUtxo() {
    return destinationUtxo;
  }

  public String getDestinationAddress() {
    return destinationAddress;
  }

  public DestinationType getDestinationType() {
    return destinationType;
  }

  public String getDestinationPath() {
    return destinationPath;
  }

  public ApiUtxoRef getFailUtxo() {
    return failUtxo;
  }

  public MixFailReason getFailReason() {
    return failReason;
  }

  public String getFailError() {
    return failError;
  }
}
