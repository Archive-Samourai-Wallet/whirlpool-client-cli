package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.client.tx0.Tx0Previews;
import java.util.Collection;
import java.util.stream.Collectors;

public class ApiTx0PreviewsResponse {
  private Collection<ApiTx0Preview> tx0Previews;

  public ApiTx0PreviewsResponse(Tx0Previews tx0Previews) {
    this.tx0Previews =
        tx0Previews.getTx0Previews().stream()
            .map(tx0Preview -> new ApiTx0Preview(tx0Preview))
            .collect(Collectors.toList());
  }

  public Collection<ApiTx0Preview> getTx0Previews() {
    return tx0Previews;
  }

  public void setTx0Previews(Collection<ApiTx0Preview> tx0Previews) {
    this.tx0Previews = tx0Previews;
  }
}
