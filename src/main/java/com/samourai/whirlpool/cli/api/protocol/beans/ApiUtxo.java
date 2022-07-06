package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.whirlpool.client.mix.listener.MixStep;
import com.samourai.whirlpool.client.wallet.beans.*;

public class ApiUtxo extends ApiUtxoDetails {
  private WhirlpoolUtxoStatus status;
  private MixStep mixStep;
  private MixableStatus mixableStatus;
  private Integer progressPercent;
  private String poolId;
  private int mixsDone;
  private boolean blocked;
  private String note;
  private String message;
  private String error;
  private Long lastActivityElapsed;

  public ApiUtxo(WhirlpoolUtxo whirlpoolUtxo) {
    super(whirlpoolUtxo);
    WhirlpoolUtxoState utxoState = whirlpoolUtxo.getUtxoState();
    this.status = utxoState.getStatus();
    this.mixStep =
        utxoState.getMixProgress() != null ? utxoState.getMixProgress().getMixStep() : null;
    this.mixableStatus = utxoState.getMixableStatus();
    this.progressPercent =
        utxoState.getMixProgress() != null
            ? utxoState.getMixProgress().getMixStep().getProgressPercent()
            : null;
    this.poolId = whirlpoolUtxo.getUtxoState().getPoolId();
    this.mixsDone = whirlpoolUtxo.getMixsDone();
    this.blocked = whirlpoolUtxo.isBlocked();
    this.note = whirlpoolUtxo.getNote();
    this.message = utxoState.getMessage();
    this.error = utxoState.getError();
    this.lastActivityElapsed =
        utxoState.getLastActivity() != null
            ? System.currentTimeMillis() - utxoState.getLastActivity()
            : null;
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

  public int getMixsDone() {
    return mixsDone;
  }

  public boolean isBlocked() {
    return blocked;
  }

  public String getNote() {
    return note;
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
