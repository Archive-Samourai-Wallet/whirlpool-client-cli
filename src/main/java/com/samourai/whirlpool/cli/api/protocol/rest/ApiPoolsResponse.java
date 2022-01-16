package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.cli.api.protocol.beans.ApiPool;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.whirlpool.beans.Pool;
import java.util.Collection;
import java.util.stream.Collectors;

public class ApiPoolsResponse {
  private Collection<ApiPool> pools;

  public ApiPoolsResponse(WhirlpoolWallet whirlpoolWallet) {
    this.pools =
        whirlpoolWallet.getPoolSupplier().getPools().stream()
            .map(pool -> computeApiPool(pool))
            .collect(Collectors.toList());
  }

  private ApiPool computeApiPool(Pool pool) {
    long tx0BalanceMin = pool.getTx0PreviewMinSpendValue();
    return new ApiPool(pool, tx0BalanceMin);
  }

  public Collection<ApiPool> getPools() {
    return pools;
  }
}
