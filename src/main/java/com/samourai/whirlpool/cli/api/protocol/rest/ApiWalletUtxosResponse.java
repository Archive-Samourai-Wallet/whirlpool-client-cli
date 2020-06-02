package com.samourai.whirlpool.cli.api.protocol.rest;

import com.google.common.primitives.Ints;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiWallet;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxoState;
import java.util.Collection;
import java.util.Comparator;
import java8.lang.Longs;

public class ApiWalletUtxosResponse {
  private ApiWallet deposit;
  private ApiWallet premix;
  private ApiWallet postmix;

  public ApiWalletUtxosResponse(WhirlpoolWallet whirlpoolWallet) throws Exception {
    Comparator<WhirlpoolUtxo> comparator =
        (o1, o2) -> {
          // last activity first
          WhirlpoolUtxoState s1 = o1.getUtxoState();
          WhirlpoolUtxoState s2 = o2.getUtxoState();
          if (s1.getLastActivity() != null || s2.getLastActivity() != null) {
            if (s1.getLastActivity() != null && s2.getLastActivity() == null) {
              return -1;
            }
            if (s2.getLastActivity() != null && s1.getLastActivity() == null) {
              return 1;
            }
            int compare = Longs.compare(s2.getLastActivity(), s1.getLastActivity());
            if (compare != 0) {
              return compare;
            }
          }

          // last confirmed
          return Ints.compare(o1.getUtxo().confirmations, o2.getUtxo().confirmations);
        };
    this.deposit = computeApiWallet(WhirlpoolAccount.DEPOSIT, whirlpoolWallet, comparator);
    this.premix = computeApiWallet(WhirlpoolAccount.PREMIX, whirlpoolWallet, comparator);
    this.postmix = computeApiWallet(WhirlpoolAccount.POSTMIX, whirlpoolWallet, comparator);
  }

  private ApiWallet computeApiWallet(
      WhirlpoolAccount account,
      WhirlpoolWallet whirlpoolWallet,
      Comparator<WhirlpoolUtxo> comparator) {
    Collection<WhirlpoolUtxo> utxos = whirlpoolWallet.getUtxoSupplier().findUtxos(account);
    String zpub = whirlpoolWallet.getWalletSupplier().getWallet(account).getZpub();
    int mixsTargetMin = whirlpoolWallet.getConfig().getMixsTarget();
    return new ApiWallet(utxos, zpub, comparator, mixsTargetMin);
  }

  public ApiWallet getDeposit() {
    return deposit;
  }

  public ApiWallet getPremix() {
    return premix;
  }

  public ApiWallet getPostmix() {
    return postmix;
  }
}
