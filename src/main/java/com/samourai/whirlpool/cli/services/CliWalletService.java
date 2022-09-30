package com.samourai.whirlpool.cli.services;

import com.google.common.eventbus.Subscribe;
import com.samourai.soroban.client.wallet.SorobanWalletService;
import com.samourai.soroban.client.wallet.counterparty.SorobanWalletCounterparty;
import com.samourai.soroban.client.wallet.sender.SorobanWalletInitiator;
import com.samourai.wallet.api.pairing.PairingDojo;
import com.samourai.wallet.bipWallet.WalletSupplier;
import com.samourai.wallet.cahoots.CahootsWallet;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import com.samourai.wallet.payload.PayloadUtilGeneric;
import com.samourai.wallet.util.SystemUtil;
import com.samourai.whirlpool.cli.beans.BusyReason;
import com.samourai.whirlpool.cli.beans.CliState;
import com.samourai.whirlpool.cli.beans.CliStatus;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.event.CliStateChangeEvent;
import com.samourai.whirlpool.cli.exception.AuthenticationException;
import com.samourai.whirlpool.cli.exception.CliRestartException;
import com.samourai.whirlpool.cli.exception.NoSessionWalletException;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.cli.utils.WalletRoutingDataSource;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import com.samourai.whirlpool.client.event.UtxosRequestEvent;
import com.samourai.whirlpool.client.event.UtxosResponseEvent;
import com.samourai.whirlpool.client.event.WalletCloseEvent;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.wallet.WhirlpoolEventService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletConfig;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletService;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bitcoinj.crypto.MnemonicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CliWalletService extends WhirlpoolWalletService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliConfig cliConfig;
  private CliConfigService cliConfigService;
  private JavaHttpClientService httpClientService;
  private JavaStompClientService stompClientService;
  private CliTorClientService cliTorClientService;
  private CliUpgradeService cliUpgradeService;
  private SorobanWalletService sorobanWalletService;
  private WalletRoutingDataSource walletRoutingDataSource;

  private Set<BusyReason> busyReasons;

  public CliWalletService(
      CliConfig cliConfig,
      CliConfigService cliConfigService,
      JavaHttpClientService httpClientService,
      JavaStompClientService stompClientService,
      CliTorClientService cliTorClientService,
      CliUpgradeService cliUpgradeService,
      SorobanWalletService sorobanWalletService,
      WalletRoutingDataSource walletRoutingDataSource) {
    super();
    this.cliConfig = cliConfig;
    this.cliConfigService = cliConfigService;
    this.httpClientService = httpClientService;
    this.stompClientService = stompClientService;
    this.cliTorClientService = cliTorClientService;
    this.cliUpgradeService = cliUpgradeService;
    this.sorobanWalletService = sorobanWalletService;
    this.walletRoutingDataSource = walletRoutingDataSource;

    this.busyReasons = new LinkedHashSet<>();

    WhirlpoolEventService.getInstance().register(this);
  }

  public CliWallet openWallet(String passphrase) throws Exception {
    // require CliStatus.READY
    if (!CliStatus.READY.equals(cliConfigService.getCliStatus())) {
      throw new NotifiableException(
          "Cannot start wallet: cliStatus=" + cliConfigService.getCliStatus());
    }

    // decrypt seed
    String seedWords;
    try {
      seedWords = CliUtils.decryptSeedWords(cliConfig.getSeed(), passphrase);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("", e);
      }
      throw new AuthenticationException("Authentication failed: invalid passphrase?");
    }

    byte[] seed;
    String walletPassphrase;
    try {
      // init wallet from seed
      seed = HD_WalletFactoryGeneric.getInstance().computeSeedFromWords(seedWords);
      walletPassphrase = cliConfig.isSeedAppendPassphrase() ? passphrase : null;
    } catch (MnemonicException e) {
      if (log.isDebugEnabled()) {
        log.debug("", e);
      }
      throw new AuthenticationException("Authentication failed: invalid passphrase?");
    }

    // debug cliConfig
    if (log.isDebugEnabled()) {
      log.debug("openWallet with cliConfig:");
      for (Map.Entry<String, String> entry : cliConfig.getConfigInfo().entrySet()) {
        log.debug("[cliConfig/" + entry.getKey() + "] " + entry.getValue());
      }
    }

    // open wallet
    WhirlpoolWalletConfig config =
        cliConfig.computeWhirlpoolWalletConfig(
            httpClientService, stompClientService, cliTorClientService, passphrase);

    CliWallet cliWallet =
        new CliWallet(
            config,
            seed,
            walletPassphrase,
            cliConfig,
            cliConfigService,
            cliTorClientService,
            httpClientService);

    // switch datasource *BEFORE* opening wallet
    walletRoutingDataSource.setDataSourceWallet(cliWallet.getWalletIdentifier(), passphrase);

    cliWallet = (CliWallet) openWallet(cliWallet, passphrase);

    // write backup
    try {
      writeBackup(cliWallet, passphrase);
    } catch (Exception e) {
      log.error("writeBackup failed", e);
    }

    // check upgrade
    boolean shouldRestart = cliUpgradeService.upgradeAuthenticated(cliWallet);
    if (shouldRestart) {
      // upgrade success => restart CLI
      log.warn(CliUtils.LOG_SEPARATOR);
      log.warn("⣿ UPGRADE SUCCESS");
      log.warn("⣿ Restarting CLI...");
      log.warn(CliUtils.LOG_SEPARATOR);
      throw new CliRestartException("Upgrade success, restarting CLI...");
    }

    // resync?
    if (cliConfig.isResync()) {
      try {
        cliWallet.resyncMixsDone();
      } catch (Exception e) {
        log.error("", e);
      }
    }
    return cliWallet;
  }

  public CliWallet getSessionWallet() throws NoSessionWalletException {
    Optional<CliWallet> cliWalletOpt = (Optional) getWhirlpoolWallet();
    if (!cliWalletOpt.isPresent()) {
      throw new NoSessionWalletException();
    }
    return cliWalletOpt.get();
  }

  public boolean hasSessionWallet() {
    return getWhirlpoolWallet().isPresent();
  }

  public CliState getCliState() {
    CliStatus cliStatus = cliConfigService.getCliStatus();
    String cliMessage = cliConfigService.getCliMessage();
    boolean loggedIn = hasSessionWallet();

    Integer torProgress = cliTorClientService.getProgress().orElse(null);
    return new CliState(cliStatus, cliMessage, loggedIn, busyReasons, torProgress);
  }

  protected File computeBackupFile(CliWallet cliWallet) throws Exception {
    // create backup dir
    File backupDir = new File(cliConfig.getBackupDirectory());
    SystemUtil.mkDir(backupDir);

    // create backup file
    // TODO zeroleak use paynymID?
    String fileName = "whirlpool-cli-backup-" + cliWallet.getWalletIdentifier() + ".txt";
    return new File(backupDir, fileName);
  }

  protected void writeBackup(CliWallet cliWallet, String password) throws Exception {
    WalletSupplier walletSupplier = cliWallet.getWalletSupplier();
    String scode = cliWallet.getConfig().getScode();
    PairingDojo pairingDojo = cliConfigService.computePairingPayload().getDojo();
    File file = computeBackupFile(cliWallet);

    if (log.isDebugEnabled()) {
      log.debug("Writing backup: " + file.getAbsolutePath());
    }
    PayloadUtilGeneric.getInstance()
        .writeBackup(walletSupplier, scode, pairingDojo, password, file);
  }

  @Subscribe
  public void onUtxosRequest(UtxosRequestEvent e) {
    this.busyAdd(BusyReason.FETCHING_WALLET);
    WhirlpoolEventService.getInstance().post(new CliStateChangeEvent());
  }

  @Subscribe
  public void onUtxosResponse(UtxosResponseEvent e) {
    this.busyRemove(BusyReason.FETCHING_WALLET);
    WhirlpoolEventService.getInstance().post(new CliStateChangeEvent());
  }

  @Subscribe
  public void onWalletClose(WalletCloseEvent e) {
    // update datasource
    walletRoutingDataSource.clearDataSourceWallet();
  }

  private void busyAdd(BusyReason busyReason) {
    this.busyReasons.add(busyReason);
    WhirlpoolEventService.getInstance().post(new CliStateChangeEvent());
  }

  private void busyRemove(BusyReason busyReason) {
    this.busyReasons.remove(busyReason);
    WhirlpoolEventService.getInstance().post(new CliStateChangeEvent());
  }

  public SorobanWalletInitiator getSorobanWalletInitiator() throws Exception {
    CahootsWallet cahootsWallet = getSessionWallet().getCahootsWallet();
    return sorobanWalletService.getSorobanWalletInitiator(cahootsWallet);
  }

  public SorobanWalletCounterparty getSorobanWalletCounterparty() throws Exception {
    CahootsWallet cahootsWallet = getSessionWallet().getCahootsWallet();
    return sorobanWalletService.getSorobanWalletCounterparty(cahootsWallet);
  }
}
