package com.samourai.whirlpool.cli.wallet;

import com.samourai.wallet.client.Bip84Wallet;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.services.CliTorClientService;
import com.samourai.whirlpool.cli.services.JavaHttpClientService;
import com.samourai.whirlpool.cli.services.WalletAggregateService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.exception.EmptyWalletException;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.mix.listener.MixSuccess;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.MixProgress;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliWallet extends WhirlpoolWallet {
  private static final Logger log = LoggerFactory.getLogger(CliWallet.class);

  private CliConfig cliConfig;
  private CliConfigService cliConfigService;
  private WalletAggregateService walletAggregateService;
  private CliTorClientService cliTorClientService;
  private JavaHttpClientService httpClientService;

  public CliWallet(
      WhirlpoolWallet whirlpoolWallet,
      CliConfig cliConfig,
      CliConfigService cliConfigService,
      WalletAggregateService walletAggregateService,
      CliTorClientService cliTorClientService,
      JavaHttpClientService httpClientService) {
    super(whirlpoolWallet);
    this.cliConfig = cliConfig;
    this.cliConfigService = cliConfigService;
    this.walletAggregateService = walletAggregateService;
    this.cliTorClientService = cliTorClientService;
    this.httpClientService = httpClientService;
  }

  @Override
  public void start() {
    if (!cliConfigService.isCliStatusReady()) {
      log.warn("Cannot start wallet: cliStatus is not ready");
      return;
    }
    // start wallet
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
  }

  @Override
  public Observable<MixProgress> mix(WhirlpoolUtxo whirlpoolUtxo) throws NotifiableException {
    // get Tor ready before mixing
    cliTorClientService.waitReady();
    return super.mix(whirlpoolUtxo);
  }

  @Override
  public void onMixSuccess(WhirlpoolUtxo whirlpoolUtxo, MixSuccess mixSuccess) {
    super.onMixSuccess(whirlpoolUtxo, mixSuccess);

    // change Tor identity
    if (cliConfig.getTor()) {
      cliTorClientService.changeIdentity();
      httpClientService.changeIdentityRest();
    }
  }

  @Override
  public synchronized void onEmptyWalletException(EmptyWalletException e) {
    try {
      if (cliConfig.isAutoAggregatePostmix()) {
        // run autoAggregatePostmix
        autoRefill(e);
      } else {
        // default management
        throw e;
      }
    } catch (Exception ee) {
      // default management
      super.onEmptyWalletException(e);
    }
  }

  private void autoRefill(EmptyWalletException e) throws Exception {
    // check balance
    long totalBalance = getUtxoSupplier().getBalanceTotal();
    if (log.isDebugEnabled()) {
      log.debug("totalBalance=" + totalBalance);
    }

    /*long missingBalance = requiredBalance - totalBalance;
    if (log.isDebugEnabled()) {
      log.debug("requiredBalance=" + requiredBalance + " => missingBalance=" + missingBalance);
    }
    if (missingBalance > 0) {
      // cannot autoAggregatePostmix
      throw new EmptyWalletException("Insufficient balance to continue", missingBalance);
    }*/

    // auto aggregate postmix is possible
    log.info(" o AutoAggregatePostmix: depositWallet wallet is empty => aggregating");
    Exception aggregateException = null;
    try {
      boolean success = walletAggregateService.consolidateWallet(this);
      if (!success) {
        throw new NotifiableException("AutoAggregatePostmix failed (nothing to aggregate?)");
      }
      if (log.isDebugEnabled()) {
        log.debug("AutoAggregatePostmix SUCCESS. ");
      }
    } catch (Exception ee) {
      // resume wallet before throwing exception (to retry later)
      aggregateException = ee;
      if (log.isDebugEnabled()) {
        log.debug("AutoAggregatePostmix ERROR, will throw error later.");
      }
    }

    // reset mixing threads to avoid mixing obsolete consolidated utxos
    mixOrchestrator.stopMixingClients();

    getUtxoSupplier().expire();

    if (aggregateException != null) {
      throw aggregateException;
    }
  }

  @Override
  public void notifyError(String message) {
    CliUtils.notifyError(message);
  }

  // make public

  @Override
  public Bip84Wallet getWalletDeposit() {
    return super.getWalletDeposit();
  }

  @Override
  public Bip84Wallet getWalletPremix() {
    return super.getWalletPremix();
  }

  @Override
  public Bip84Wallet getWalletPostmix() {
    return super.getWalletPostmix();
  }
}
