package com.samourai.whirlpool.cli.run;

import com.samourai.whirlpool.cli.ApplicationArgs;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunCliCommand {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ApplicationArgs appArgs;
  private CliWalletService cliWalletService;

  public RunCliCommand(ApplicationArgs appArgs, CliWalletService cliWalletService) {
    this.appArgs = appArgs;
    this.cliWalletService = cliWalletService;
  }

  public void run() throws Exception {
    if (appArgs.isDumpPayload()) {
      new RunDumpPayload(cliWalletService).run();
    } else if (appArgs.isAggregate()) {
      CliWallet cliWallet = cliWalletService.getSessionWallet();

      String toAddress = appArgs.getAggregate();
      if (toAddress != null && !"true".equals(toAddress)) {
        // aggregate to a specific address
        cliWallet.aggregateTo(toAddress);
      } else {
        // aggregate
        cliWallet.aggregate();
      }
    } else if (appArgs.isListPools()) {
      CliWallet cliWallet = cliWalletService.getSessionWallet();
      new RunListPools(cliWallet).run();
    } else {
      throw new Exception("Unknown command.");
    }

    if (log.isDebugEnabled()) {
      log.debug("RunCliCommand success.");
    }
  }

  public static String getCommandToRun(ApplicationArgs appArgs) {
    if (appArgs.isDumpPayload()) {
      return ApplicationArgs.ARG_DUMP_PAYLOAD;
    }
    if (appArgs.isAggregate()) {
      return ApplicationArgs.ARG_AGGREGATE;
    }
    if (appArgs.isListPools()) {
      return appArgs.ARG_LIST_POOLS;
    }
    return null;
  }
}
