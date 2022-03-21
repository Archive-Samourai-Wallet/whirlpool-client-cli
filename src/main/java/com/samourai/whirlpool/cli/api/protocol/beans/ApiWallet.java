package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ApiWallet {
  private Collection<ApiUtxo> utxos;
  private Collection<ApiTx> txs;
  private long balance;
  private String zpub;

  public ApiWallet(
      WhirlpoolAccount account,
      WhirlpoolWallet whirlpoolWallet,
      Comparator<WhirlpoolUtxo> comparator) {
    Collection<WhirlpoolUtxo> utxos = whirlpoolWallet.getUtxoSupplier().findUtxos(account);
    this.utxos =
        utxos.stream()
            .sorted(comparator)
            .map(whirlpoolUtxo -> new ApiUtxo(whirlpoolUtxo))
            .collect(Collectors.toList());
    this.txs =
        whirlpoolWallet.getUtxoSupplier().findTxs(account).stream()
            .map(tx -> new ApiTx(tx))
            .collect(Collectors.toList());
    this.balance = WhirlpoolUtxo.sumValue(utxos);
    BipWallet bip84Wallet =
        whirlpoolWallet.getWalletSupplier().getWallet(account, BIP_FORMAT.SEGWIT_NATIVE);
    this.zpub = bip84Wallet.getPub();
  }

  public Collection<ApiUtxo> getUtxos() {
    return utxos;
  }

  public Collection<ApiTx> getTxs() {
    return txs;
  }

  public long getBalance() {
    return balance;
  }

  public String getZpub() {
    return zpub;
  }
}
