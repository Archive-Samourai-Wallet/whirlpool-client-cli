package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.cli.api.protocol.beans.ApiMixHistory;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxo;
import com.samourai.whirlpool.client.wallet.beans.MixHistory;
import com.samourai.whirlpool.client.wallet.beans.MixingState;
import java.util.Collection;
import java.util.stream.Collectors;

public class ApiWalletStateResponse {
  private boolean started;
  private int nbMixing;
  private int nbQueued;
  private Collection<ApiUtxo> threads;
  private ApiMixHistory mixHistory;

  public ApiWalletStateResponse(
      MixingState mixingState, MixHistory mixHistory, int latestBlockHeight) {
    this.started = mixingState.isStarted();
    this.nbMixing = mixingState.getNbMixing();
    this.nbQueued = mixingState.getNbQueued();
    this.threads =
        mixingState.getUtxosMixing().stream()
            .map(whirlpoolUtxo -> new ApiUtxo(whirlpoolUtxo, latestBlockHeight))
            .collect(Collectors.toList());
    this.mixHistory = new ApiMixHistory(mixHistory);
  }

  public boolean isStarted() {
    return started;
  }

  public int getNbMixing() {
    return nbMixing;
  }

  public int getNbQueued() {
    return nbQueued;
  }

  public Collection<ApiUtxo> getThreads() {
    return threads;
  }

  public ApiMixHistory getMixHistory() {
    return mixHistory;
  }
}
