package com.samourai.whirlpool.cli.run;

import com.samourai.wallet.util.AbstractOrchestrator;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.exception.NoSessionWalletException;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.utils.DebugUtils;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliStatusInteractiveOrchestrator extends AbstractOrchestrator {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliWalletService cliWalletService;
  private CliConfig cliConfig;

  public CliStatusInteractiveOrchestrator(
      int loopDelay, CliWalletService cliWalletService, CliConfig cliConfig) {
    super(loopDelay);
    this.cliWalletService = cliWalletService;
    this.cliConfig = cliConfig;
  }

  @Override
  protected void runOrchestrator() {
    interactive();
  }

  private void interactive() {
    while (isStarted()) {
      try {
        Character car = CliUtils.readChar();
        if (car != null) {
          car = Character.toUpperCase(car);
          if (car.equals('T')) {
            printThreads();
          } else if (car.equals('D')) {
            printUtxos(WhirlpoolAccount.DEPOSIT);
          } else if (car.equals('P')) {
            printUtxos(WhirlpoolAccount.PREMIX);
          } else if (car.equals('O')) {
            printUtxos(WhirlpoolAccount.POSTMIX);
          } else if (car.equals('W')) {
            printWallet();
          } else if (car.equals('B')) {
            printDebug();
          } else if (car.equals('L')) {
            printPools();
          } else if (car.equals('H')) {
            printMixHistory();
          } else if (car.equals('X')) {
            printXPubHistory();
          } else if (car.equals('C')) {
            printCoordinators();
          }
        } else {
          if (log.isDebugEnabled()) {
            log.debug("console input was null");
          }
        }
      } catch (Exception e) {
        log.error("", e);
      }
    }
  }

  private void printThreads() {
    try {
      WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
      log.info(DebugUtils.getDebugMixingThreads(whirlpoolWallet));
    } catch (NoSessionWalletException e) {
      System.out.print("⣿ Wallet CLOSED\r");
    }
  }

  private void printUtxos(WhirlpoolAccount account) throws Exception {
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    Collection<WhirlpoolUtxo> utxos = whirlpoolWallet.getUtxoSupplier().findUtxos(account);
    int latestBlockHeight = whirlpoolWallet.getChainSupplier().getLatestBlock().height;

    log.info("⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿");
    log.info("⣿ " + account.name() + " UTXOS:");
    log.info(DebugUtils.getDebugUtxos(utxos, latestBlockHeight));
  }

  private void printWallet() throws Exception {
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    log.info(DebugUtils.getDebugWallet(whirlpoolWallet));
  }

  private void printDebug() throws Exception {
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    log.info(DebugUtils.getDebug(whirlpoolWallet));
  }

  private void printPools() throws Exception {
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    log.info(DebugUtils.getDebugPools(whirlpoolWallet.getPoolSupplier()));
  }

  private void printMixHistory() throws Exception {
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    log.info(DebugUtils.getDebugMixHistory(whirlpoolWallet));
  }

  private void printXPubHistory() throws Exception {
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    if (!cliConfig.isExternalDestinationConfigured()) {
      log.error("No external XPub configured yet. Use --set-external-xpub to configure.");
      return;
    }
    if (!cliConfig.isExternalDestinationEnabled()) {
      log.error("External XPub is DISABLED. Use --set-external-xpub-enabled=true to enable.");
      return;
    }
    log.info(DebugUtils.getDebugXPubHistory(whirlpoolWallet));
  }

  private void printCoordinators() throws Exception {
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    log.info(DebugUtils.getDebugCoordinators(whirlpoolWallet.getCoordinatorSupplier()));
  }
}
