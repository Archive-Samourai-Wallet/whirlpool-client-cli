package com.samourai.whirlpool.cli.run;

import com.samourai.whirlpool.cli.ApplicationArgs;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import java.lang.invoke.MethodHandles;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunCliCommand {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ApplicationArgs appArgs;
  private CliWalletService cliWalletService;
  private CliConfigService cliConfigService;
  private CliConfig cliConfig;

  public RunCliCommand(
      ApplicationArgs appArgs,
      CliWalletService cliWalletService,
      CliConfigService cliConfigService,
      CliConfig cliConfig) {
    this.appArgs = appArgs;
    this.cliWalletService = cliWalletService;
    this.cliConfigService = cliConfigService;
    this.cliConfig = cliConfig;
  }

  public void run(NetworkParameters params, String seedPassphrase) throws Exception {
    CliWallet cliWallet = cliWalletService.getSessionWallet();
    if (appArgs.isDumpPayload()) {
      new RunDumpPayload(cliWalletService).run();
    } else if (appArgs.isAggregate()) {
      String toAddress = appArgs.getAggregate();
      if (toAddress != null && !"true".equals(toAddress)) {
        // aggregate to a specific address
        cliWallet.aggregateTo(toAddress);
      } else {
        // aggregate
        cliWallet.aggregate();
      }
    } else if (appArgs.isListPools()) {
      new RunListPools(cliWallet).run();
    } else if (appArgs.isSetExternalXpub()) {
      // set-external-xpub
      new RunSetExternalXpub(cliConfigService).run(params, cliWallet, seedPassphrase);
    } else if (appArgs.isSetExternalXpubEnabled()) {
      // enable/disable external-xpub
      boolean enabled = appArgs.getSetExternalXpubEnabled();
      new RunSetExternalXpubEnabled(cliConfigService).run(cliConfig, enabled);
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
    if (appArgs.isSetExternalXpub()) {
      return appArgs.ARG_SET_EXTERNAL_XPUB;
    }
    if (appArgs.isSetExternalXpubEnabled()) {
      return appArgs.ARG_SET_EXTERNAL_XPUB_ENABLED;
    }
    return null;
  }
}
