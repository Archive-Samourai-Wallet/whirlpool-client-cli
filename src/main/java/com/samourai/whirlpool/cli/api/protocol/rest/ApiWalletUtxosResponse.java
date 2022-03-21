package com.samourai.whirlpool.cli.api.protocol.rest;

import com.google.common.primitives.Ints;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiWallet;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxoState;
import java.util.Comparator;

public class ApiWalletUtxosResponse {
  private ApiWallet deposit;
  private ApiWallet premix;
  private ApiWallet postmix;
  private long balance;
  private long lastUpdate;

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
            int compare = Long.compare(s2.getLastActivity(), s1.getLastActivity());
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
    this.balance = this.deposit.getBalance() + this.premix.getBalance() + this.postmix.getBalance();
    this.lastUpdate = whirlpoolWallet.getUtxoSupplier().getLastUpdate();
  }

  private ApiWallet computeApiWallet(
      WhirlpoolAccount account,
      WhirlpoolWallet whirlpoolWallet,
      Comparator<WhirlpoolUtxo> comparator) {
    return new ApiWallet(account, whirlpoolWallet, comparator);
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

  public long getBalance() {
    return balance;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }
}
