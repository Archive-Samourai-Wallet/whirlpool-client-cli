package com.samourai.whirlpool.cli.run;

import com.samourai.whirlpool.cli.services.CliArgs;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunCliCommand {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliArgs appArgs;
  private CliWalletService cliWalletService;
  private CliConfigService cliConfigService;

  public RunCliCommand(
      CliArgs appArgs, CliWalletService cliWalletService, CliConfigService cliConfigService) {
    this.appArgs = appArgs;
    this.cliWalletService = cliWalletService;
    this.cliConfigService = cliConfigService;
  }

  public void run() throws Exception {
    if (appArgs.isDumpPayload()) {
      new RunDumpPayload(cliConfigService).run();
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

  public static String getCommandToRun(CliArgs appArgs) {
    if (appArgs.isDumpPayload()) {
      return CliArgs.ARG_DUMP_PAYLOAD;
    }
    if (appArgs.isAggregate()) {
      return CliArgs.ARG_AGGREGATE;
    }
    if (appArgs.isListPools()) {
      return appArgs.ARG_LIST_POOLS;
    }
    return null;
  }
}
