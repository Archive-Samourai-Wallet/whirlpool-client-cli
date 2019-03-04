package com.samourai.whirlpool.cli.services;

import com.samourai.wallet.client.indexHandler.FileIndexHandler;
import com.samourai.wallet.client.indexHandler.IIndexHandler;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.java.HD_WalletFactoryJava;
import com.samourai.whirlpool.cli.beans.CliStatus;
import com.samourai.whirlpool.cli.beans.Encrypted;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.exception.NoSessionWalletException;
import com.samourai.whirlpool.cli.run.RunUpgradeCli;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.cli.utils.EncryptUtils;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletService;
import java.io.File;
import java.lang.invoke.MethodHandles;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.MnemonicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CliWalletService extends WhirlpoolWalletService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final int CLI_VERSION = 3;

  private static final String INDEX_BIP84_INITIALIZED = "bip84init";
  private static final String INDEX_DEPOSIT = "deposit";
  private static final String INDEX_DEPOSIT_CHANGE = "deposit_change";
  private static final String INDEX_PREMIX = "premix";
  private static final String INDEX_PREMIX_CHANGE = "premix_change";
  private static final String INDEX_POSTMIX = "postmix";
  private static final String INDEX_POSTMIX_CHANGE = "postmix_change";
  private static final String INDEX_FEE = "fee";
  private static final String INDEX_CLI_VERSION = "cliVersion";

  private CliConfig cliConfig;
  private CliConfigService cliConfigService;
  private FileIndexHandler fileIndexHandler;
  private HD_WalletFactoryJava hdWalletFactory;
  private WalletAggregateService walletAggregateService;

  // available when wallet is opened
  private CliWallet sessionWallet = null;

  public CliWalletService(
      CliConfig cliConfig,
      CliConfigService cliConfigService,
      HD_WalletFactoryJava hdWalletFactory,
      WalletAggregateService walletAggregateService) {
    super(cliConfig.computeWhirlpoolWalletConfig());
    this.cliConfig = cliConfig;
    this.cliConfigService = cliConfigService;
    this.hdWalletFactory = hdWalletFactory;
    this.walletAggregateService = walletAggregateService;
  }

  public WhirlpoolWallet openWallet(String seedPassphrase) throws Exception {
    // require CliStatus.READY
    if (!CliStatus.READY.equals(cliConfigService.getCliStatus())) {
      throw new NotifiableException(
          "Cannot start wallet: cliStatus=" + cliConfigService.getCliStatus());
    }

    NetworkParameters params = cliConfig.getServer().getParams();

    String seedWords;
    try {
      seedWords = decryptSeedWords(seedPassphrase);
    } catch (Exception e) {
      log.error("decryptSeedWords failed, invalid passphrase?", e);
      throw new NotifiableException("Seed decrypt failed, invalid passphrase?");
    }

    // init fileIndexHandler
    String seedEncrypted = cliConfig.getSeed();
    String walletIdentifier = CliUtils.sha256Hash(seedPassphrase + seedEncrypted + params.getId());
    this.fileIndexHandler = new FileIndexHandler(computeIndexFile(walletIdentifier));

    HD_Wallet bip84w;
    try {
      // init wallet from seed
      byte[] seed = hdWalletFactory.computeSeedFromWords(seedWords);
      bip84w = hdWalletFactory.getBIP84(seed, seedPassphrase, params);
    } catch (MnemonicException e) {
      throw new NotifiableException(
          "Invalid seed. You may want to reset CLI configuration and setup another seed.", e);
    }

    // init bip84 at first run
    boolean initBip84 = (fileIndexHandler.get(INDEX_BIP84_INITIALIZED) != 1);

    // deposit, premix & postmix wallet indexs
    IIndexHandler depositIndexHandler = fileIndexHandler.getIndexHandler(INDEX_DEPOSIT);
    IIndexHandler depositChangeIndexHandler =
        fileIndexHandler.getIndexHandler(INDEX_DEPOSIT_CHANGE);
    IIndexHandler premixIndexHandler = fileIndexHandler.getIndexHandler(INDEX_PREMIX);
    IIndexHandler premixChangeIndexHandler = fileIndexHandler.getIndexHandler(INDEX_PREMIX_CHANGE);
    IIndexHandler postmixIndexHandler = fileIndexHandler.getIndexHandler(INDEX_POSTMIX);
    IIndexHandler postmixChangeIndexHandler =
        fileIndexHandler.getIndexHandler(INDEX_POSTMIX_CHANGE);
    IIndexHandler feeIndexHandler = fileIndexHandler.getIndexHandler(INDEX_FEE);

    // services
    WhirlpoolWallet whirlpoolWallet =
        openWallet(
            bip84w,
            depositIndexHandler,
            depositChangeIndexHandler,
            premixIndexHandler,
            premixChangeIndexHandler,
            postmixIndexHandler,
            postmixChangeIndexHandler,
            feeIndexHandler,
            initBip84);

    if (initBip84) {
      // save initialized state
      fileIndexHandler.set(INDEX_BIP84_INITIALIZED, 1);
    }

    this.sessionWallet = new CliWallet(whirlpoolWallet, cliConfig, walletAggregateService, this);

    // check upgrade wallet
    checkUpgradeWallet();

    return sessionWallet;
  }

  public boolean checkSeedValid(String seedWords, String seedPassphrase) throws Exception {
    NetworkParameters params = cliConfig.getServer().getParams();
    try {
      // init wallet from seed
      byte[] seed = hdWalletFactory.computeSeedFromWords(seedWords);
      hdWalletFactory.getBIP84(seed, seedPassphrase, params);
      return true;
    } catch (MnemonicException e) {
      log.error("", e);
      return false;
    }
  }

  private String decryptSeedWords(String seedPassphrase) throws Exception {
    String seedWordsEncrypted = cliConfig.getSeed();
    return EncryptUtils.decrypt(seedPassphrase, seedWordsEncrypted);
  }

  public Encrypted encryptSeedWords(String seedWords, String seedPassphrase) throws Exception {
    return EncryptUtils.encrypt(seedPassphrase, seedWords);
  }

  public void closeWallet() {
    if (this.sessionWallet != null) {
      this.sessionWallet.stop();
      this.sessionWallet = null;
    }
  }

  public CliWallet getSessionWallet() throws NoSessionWalletException {
    if (sessionWallet == null) {
      throw new NoSessionWalletException();
    }
    return sessionWallet;
  }

  public boolean hasSessionWallet() {
    return sessionWallet != null;
  }

  private File computeIndexFile(String walletIdentifier) throws NotifiableException {
    String path = "whirlpool-cli-state-" + walletIdentifier + ".json";
    if (log.isDebugEnabled()) {
      log.debug("indexFile: " + path);
    }
    File f = new File(path);
    if (!f.exists()) {
      if (log.isDebugEnabled()) {
        log.debug("Creating file " + path);
      }
      try {
        f.createNewFile();
      } catch (Exception e) {
        throw new NotifiableException("Unable to write file " + path);
      }
    }
    return f;
  }

  private void checkUpgradeWallet() throws Exception {
    IIndexHandler cliVersionHandler =
        fileIndexHandler.getIndexHandler(INDEX_CLI_VERSION, CLI_VERSION);
    int lastVersion = cliVersionHandler.get();

    if (lastVersion == CLI_VERSION) {
      // up to date
      if (log.isDebugEnabled()) {
        log.debug("cli wallet is up to date: " + CLI_VERSION);
      }
      return;
    }

    if (log.isDebugEnabled()) {
      log.debug(" • Upgrading cli wallet: " + lastVersion + " -> " + CLI_VERSION);
    }
    new RunUpgradeCli(this).run(lastVersion);

    // set new version
    cliVersionHandler.set(CLI_VERSION);
  }
}
