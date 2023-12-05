package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.cli.api.protocol.beans.ApiMixResult;
import com.samourai.whirlpool.client.wallet.beans.MixResult;
import java.util.Collection;
import java.util.stream.Collectors;

public class ApiMixHistoryResponse {
  private Collection<ApiMixResult> mixResults;

  public ApiMixHistoryResponse(Collection<MixResult> mixResults) {
    this.mixResults =
        mixResults.stream().map(o -> new ApiMixResult(o)).collect(Collectors.toList());
  }

  public Collection<ApiMixResult> getMixResults() {
    return mixResults;
  }
}
