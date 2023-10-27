package com.samourai.whirlpool.cli.run;

import com.samourai.whirlpool.cli.wallet.CliWallet;
import com.samourai.whirlpool.client.utils.DebugUtils;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunListPools {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliWallet cliWallet;

  public RunListPools(CliWallet cliWallet) {
    this.cliWallet = cliWallet;
  }

  public void run() throws Exception {
    log.info(DebugUtils.getDebugPools(cliWallet.getPoolSupplier()));
  }
}
