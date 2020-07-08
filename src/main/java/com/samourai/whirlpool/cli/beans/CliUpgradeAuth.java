package com.samourai.whirlpool.cli.beans;

import com.samourai.whirlpool.cli.wallet.CliWallet;

public abstract class CliUpgradeAuth extends CliUpgrade {

  public CliUpgradeAuth() {
    super();
  }

  public abstract void run(CliWallet cliWallet) throws Exception;
}
