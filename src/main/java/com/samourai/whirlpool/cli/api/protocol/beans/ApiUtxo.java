package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.wallet.api.backend.beans.UnspentResponse;
import com.samourai.whirlpool.client.mix.listener.MixStep;
import com.samourai.whirlpool.client.wallet.beans.*;

public class ApiUtxo {
  private String hash;
  private int index;
  private long value;
  private int confirmations;
  private String path;

  private WhirlpoolAccount account;
  private WhirlpoolUtxoStatus status;
  private MixStep mixStep;
  private MixableStatus mixableStatus;
  private Integer progressPercent;
  private String poolId;
  private Integer mixsTarget;
  private Integer mixsTargetOrDefault;
  private int mixsDone;
  private String message;
  private String error;
  private Long lastActivityElapsed;

  public ApiUtxo(WhirlpoolUtxo whirlpoolUtxo, int mixsTargetMin) {
    UnspentResponse.UnspentOutput utxo = whirlpoolUtxo.getUtxo();
    this.hash = utxo.tx_hash;
    this.index = utxo.tx_output_n;
    this.value = utxo.value;
    this.confirmations = utxo.confirmations;
    this.path = utxo.xpub.path;

    this.account = whirlpoolUtxo.getAccount();
    WhirlpoolUtxoState utxoState = whirlpoolUtxo.getUtxoState();
    this.status = utxoState.getStatus();
    this.mixStep =
        utxoState.getMixProgress() != null ? utxoState.getMixProgress().getMixStep() : null;
    this.mixableStatus = utxoState.getMixableStatus();
    this.progressPercent =
        utxoState.getMixProgress() != null ? utxoState.getMixProgress().getProgressPercent() : null;
    this.poolId = whirlpoolUtxo.getPoolId();
    this.mixsTarget = whirlpoolUtxo.getMixsTarget();
    this.mixsTargetOrDefault = whirlpoolUtxo.getMixsTargetOrDefault(mixsTargetMin);
    this.mixsDone = whirlpoolUtxo.getMixsDone();
    this.message = utxoState.getMessage();
    this.error = utxoState.getError();
    this.lastActivityElapsed =
        utxoState.getLastActivity() != null
            ? System.currentTimeMillis() - utxoState.getLastActivity()
            : null;
  }

  public String getHash() {
    return hash;
  }

  public int getIndex() {
    return index;
  }

  public long getValue() {
    return value;
  }

  public int getConfirmations() {
    return confirmations;
  }

  public String getPath() {
    return path;
  }

  public WhirlpoolAccount getAccount() {
    return account;
  }

  public WhirlpoolUtxoStatus getStatus() {
    return status;
  }

  public MixStep getMixStep() {
    return mixStep;
  }

  public MixableStatus getMixableStatus() {
    return mixableStatus;
  }

  public Integer getProgressPercent() {
    return progressPercent;
  }

  public String getPoolId() {
    return poolId;
  }

  public Integer getMixsTarget() {
    return mixsTarget;
  }

  public Integer getMixsTargetOrDefault() {
    return mixsTargetOrDefault;
  }

  public int getMixsDone() {
    return mixsDone;
  }

  public String getMessage() {
    return message;
  }

  public String getError() {
    return error;
  }

  public Long getLastActivityElapsed() {
    return lastActivityElapsed;
  }
}
