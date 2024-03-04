package com.samourai.whirlpool.cli.run;

import com.samourai.wallet.constants.SamouraiAccount;
import com.samourai.wallet.util.AbstractOrchestrator;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.exception.NoSessionWalletException;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletConfig;
import com.samourai.whirlpool.client.wallet.beans.MixHistory;
import com.samourai.whirlpool.client.wallet.beans.MixingState;
import com.samourai.whirlpool.client.wallet.data.utxo.UtxoSupplier;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliStatusOrchestrator extends AbstractOrchestrator {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliStatusInteractiveOrchestrator statusInteractiveOrchestrator;
  private CliWalletService cliWalletService;
  private CliConfig cliConfig;

  public CliStatusOrchestrator(
      int loopDelay, CliWalletService cliWalletService, CliConfig cliConfig) {
    super(loopDelay);
    if (CliUtils.hasConsole()) {
      this.statusInteractiveOrchestrator =
          new CliStatusInteractiveOrchestrator(loopDelay, cliWalletService, cliConfig);
    }
    this.cliWalletService = cliWalletService;
    this.cliConfig = cliConfig;
  }

  @Override
  public synchronized void start(boolean daemon) {
    super.start(daemon);
    if (statusInteractiveOrchestrator != null) {
      statusInteractiveOrchestrator.start(true);
    }
  }

  @Override
  public synchronized void stop() {
    super.stop();
    if (statusInteractiveOrchestrator != null) {
      statusInteractiveOrchestrator.stop();
    }
  }

  @Override
  protected void runOrchestrator() {
    printState();
  }

  private void printState() {
    try {
      WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
      MixingState mixingState = whirlpoolWallet.getMixingState();
      MixHistory mixHistory = whirlpoolWallet.getMixHistory();
      WhirlpoolWalletConfig walletConfig = whirlpoolWallet.getConfig();
      UtxoSupplier utxoSupplier = whirlpoolWallet.getUtxoSupplier();

      int nbDeposit = utxoSupplier.findUtxos(SamouraiAccount.DEPOSIT).size();
      int nbPremix = utxoSupplier.findUtxos(SamouraiAccount.PREMIX).size();
      int nbPostmix = utxoSupplier.findUtxos(SamouraiAccount.POSTMIX).size();
      double balanceTotal = ClientUtils.satToBtc(utxoSupplier.getBalanceTotal());

      int nbPools = whirlpoolWallet.getPoolSupplier().getPools().size();
      int nbCoordinators = whirlpoolWallet.getCoordinatorSupplier().getCoordinators().size();

      System.out.print(
          "⣿ Whirlpool "
              + (mixingState.isStarted() ? "STARTED" : "STOPPED")
              + (walletConfig.isAutoTx0() ? " +autoTx0=" + walletConfig.getAutoTx0PoolId() : "")
              + (walletConfig.isAutoMix() ? " +autoMix" : "")
              + (cliConfig.getTor() ? " +Tor" : "")
              + (cliConfig.isDojoEnabled() ? " +Dojo" : "")
              + (walletConfig.getExternalDestination() != null
                  ? " +XPub(" + ClientUtils.satToBtc(mixHistory.getExternalXpubVolume()) + ")"
                  : "")
              + ", "
              + mixingState.getNbMixing()
              + " mixing ("
              + mixingState.getNbMixingMustMix()
              + "+"
              + mixingState.getNbMixingLiquidity()
              + "), "
              + mixingState.getNbQueued()
              + " queued ("
              + mixingState.getNbQueuedMustMix()
              + "+"
              + mixingState.getNbQueuedLiquidity()
              + "), "
              + balanceTotal
              + " BTC. [T]hreads("
              + mixingState.getNbMixing()
              + "), [D]eposit("
              + nbDeposit
              + "), [P]remix("
              + nbPremix
              + "), P[O]stmix("
              + nbPostmix
              + "), "
              + (whirlpoolWallet.getConfig().getExternalDestination() != null
                  ? "[X]Pub(" + mixHistory.getExternalXpubCount() + "), "
                  : "")
              + "[H]istory("
              + mixHistory.getMixedCount()
              + "), [W]allet, , POO[L]S("
              + nbPools
              + "), [C]OORDINATORS("
              + nbCoordinators
              + "), DE[B]UG\r");
    } catch (NoSessionWalletException e) {
      System.out.print("⣿ Wallet CLOSED\r");
    } catch (Exception e) {
      log.error("", e);
    }
  }
}
