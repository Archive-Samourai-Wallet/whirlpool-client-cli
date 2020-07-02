package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ApiWallet {
  private Collection<ApiUtxo> utxos;
  private long lastUpdate;
  private long balance;
  private String zpub;

  public ApiWallet(
      Collection<WhirlpoolUtxo> whirlpoolUtxos,
      long lastUpdate,
      String zpub,
      Comparator<WhirlpoolUtxo> comparator,
      int mixsTargetMin) {
    this.utxos =
        whirlpoolUtxos
            .stream()
            .sorted(comparator)
            .map(whirlpoolUtxo -> new ApiUtxo(whirlpoolUtxo, mixsTargetMin))
            .collect(Collectors.toList());
    this.lastUpdate = lastUpdate;
    this.balance =
        whirlpoolUtxos.stream().mapToLong(whirlpoolUtxo -> whirlpoolUtxo.getUtxo().value).sum();
    this.zpub = zpub;
  }

  public Collection<ApiUtxo> getUtxos() {
    return utxos;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public long getBalance() {
    return balance;
  }

  public String getZpub() {
    return zpub;
  }
}
