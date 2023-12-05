package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.cli.api.protocol.beans.ApiMixHistory;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiMixResult;
import com.samourai.whirlpool.client.wallet.beans.MixHistory;
import java.util.Collection;
import java.util.stream.Collectors;

public class ApiMixHistoryResponse {
  private Collection<ApiMixResult> mixResults;
  private ApiMixHistory mixHistory;

  public ApiMixHistoryResponse(MixHistory mixHistory) {
    this.mixResults =
        mixHistory.getMixResultsDesc().stream()
            .map(o -> new ApiMixResult(o))
            .collect(Collectors.toList());
    this.mixHistory = new ApiMixHistory(mixHistory);
  }

  public Collection<ApiMixResult> getMixResults() {
    return mixResults;
  }

  public ApiMixHistory getMixHistory() {
    return mixHistory;
  }
}
