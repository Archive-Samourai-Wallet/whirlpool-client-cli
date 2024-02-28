package com.samourai.whirlpool.cli.services;

import com.google.common.eventbus.Subscribe;
import com.samourai.soroban.client.wallet.SorobanWalletService;
import com.samourai.wallet.api.pairing.PairingDojo;
import com.samourai.wallet.api.pairing.PairingNetwork;
import com.samourai.wallet.api.pairing.PairingPayload;
import com.samourai.wallet.bip47.BIP47UtilGeneric;
import com.samourai.wallet.bip47.rpc.secretPoint.ISecretPointFactory;
import com.samourai.wallet.crypto.AESUtil;
import com.samourai.wallet.crypto.CryptoUtil;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import com.samourai.wallet.util.CharSequenceX;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.whirlpool.cli.beans.BusyReason;
import com.samourai.whirlpool.cli.beans.CliState;
import com.samourai.whirlpool.cli.beans.CliStatus;
import com.samourai.whirlpool.cli.beans.WhirlpoolPairingPayload;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.config.CliConfigFile;
import com.samourai.whirlpool.cli.event.CliStateChangeEvent;
import com.samourai.whirlpool.cli.exception.AuthenticationException;
import com.samourai.whirlpool.cli.exception.CliRestartException;
import com.samourai.whirlpool.cli.exception.NoSessionWalletException;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import com.samourai.whirlpool.client.event.UtxosRequestEvent;
import com.samourai.whirlpool.client.event.UtxosResponseEvent;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.wallet.WhirlpoolEventService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletConfig;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletService;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.crypto.MnemonicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CliWalletService extends WhirlpoolWalletService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final FormatsUtilGeneric formatUtils = FormatsUtilGeneric.getInstance();

  private ISecretPointFactory secretPointFactory;
  private CryptoUtil cryptoUtil;
  private SorobanWalletService sorobanWalletService;
  private CliConfig cliConfig;
  private CliConfigService cliConfigService;
  private JavaHttpClientService httpClientService;
  private CliTorClientService cliTorClientService;
  private BIP47UtilGeneric bip47Util;
  private CliUpgradeService cliUpgradeService;

  private Set<BusyReason> busyReasons;

  public CliWalletService(
      ISecretPointFactory secretPointFactory,
      CryptoUtil cryptoUtil,
      SorobanWalletService sorobanWalletService,
      CliConfig cliConfig,
      CliConfigService cliConfigService,
      JavaHttpClientService httpClientService,
      CliTorClientService cliTorClientService,
      BIP47UtilGeneric bip47Util,
      CliUpgradeService cliUpgradeService) {
    super();
    this.secretPointFactory = secretPointFactory;
    this.cryptoUtil = cryptoUtil;
    this.sorobanWalletService = sorobanWalletService;
    this.cliConfig = cliConfig;
    this.cliConfigService = cliConfigService;
    this.httpClientService = httpClientService;
    this.cliTorClientService = cliTorClientService;
    this.bip47Util = bip47Util;
    this.cliUpgradeService = cliUpgradeService;

    this.busyReasons = new LinkedHashSet<>();

    WhirlpoolEventService.getInstance().register(this);
  }

  public CliWallet openWallet(String passphrase) throws Exception {
    // require CliStatus.READY
    if (!CliStatus.READY.equals(cliConfigService.getCliStatus())) {
      throw new NotifiableException(
          "Cannot start wallet: cliStatus=" + cliConfigService.getCliStatus());
    }

    cliConfigService.setCliStatus(CliStatus.STARTING);

    CliWallet cliWallet;
    try {
      // decrypt seed
      String seedWords;
      try {
        seedWords = decryptSeedWords(cliConfig.getSeed(), passphrase);
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
              secretPointFactory,
              cryptoUtil,
              sorobanWalletService,
              httpClientService,
              bip47Util,
              passphrase);

      cliWallet =
          new CliWallet(
              config,
              seed,
              walletPassphrase,
              cliConfig,
              cliConfigService,
              cliTorClientService,
              httpClientService);
      cliWallet = (CliWallet) openWallet(cliWallet, passphrase);
    } catch (Exception e) {
      cliConfigService.setCliStatus(CliStatus.READY); // revert to READY status
      throw e;
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

    // ready
    cliConfigService.setCliStatus(CliStatus.READY);
    return cliWallet;
  }

  protected String decryptSeedWords(String seedWordsEncrypted, String seedPassphrase)
      throws Exception {
    String decrypted = AESUtil.decrypt(seedWordsEncrypted, new CharSequenceX(seedPassphrase));
    if (StringUtils.isEmpty(decrypted)) {
      throw new Exception("Invalid passphrase");
    }
    return decrypted;
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

  public String computePairingPayload() throws Exception {
    PairingNetwork pairingNetwork =
        formatUtils.isTestNet(cliConfig.getWhirlpoolNetwork().getParams())
            ? PairingNetwork.TESTNET
            : PairingNetwork.MAINNET;

    PairingDojo dojo = null;
    CliConfigFile.DojoConfig dojoConfig = cliConfig.getDojo();
    if (dojoConfig.isEnabled()) {
      dojo = cliConfigService.computePairingDojo(dojoConfig.getUrl(), dojoConfig.getApiKey());
    }

    String seedEncrypted = cliConfig.getSeed();
    PairingPayload pairingPayload =
        new WhirlpoolPairingPayload(
            pairingNetwork, seedEncrypted, cliConfig.isSeedAppendPassphrase(), dojo);
    String json = ClientUtils.toJsonString(pairingPayload);
    return json;
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

  private void busyAdd(BusyReason busyReason) {
    this.busyReasons.add(busyReason);
    WhirlpoolEventService.getInstance().post(new CliStateChangeEvent());
  }

  private void busyRemove(BusyReason busyReason) {
    this.busyReasons.remove(busyReason);
    WhirlpoolEventService.getInstance().post(new CliStateChangeEvent());
  }
}
