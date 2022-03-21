package com.samourai.whirlpool.cli.run;

import com.samourai.wallet.api.pairing.PairingDojo;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import com.samourai.wallet.payload.BackupPayload;
import com.samourai.wallet.payload.PayloadUtilGeneric;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.whirlpool.cli.beans.WhirlpoolPairingPayload;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import java.io.File;
import java.lang.invoke.MethodHandles;
import org.aspectj.util.FileUtil;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

public class RunCliInit {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int PASSPHRASE_MINLENGTH = 4;

  private CliConfigService cliConfigService;
  private CliConfig cliConfig;

  public RunCliInit(CliConfigService cliConfigService, CliConfig cliConfig) {
    this.cliConfigService = cliConfigService;
    this.cliConfig = cliConfig;
  }

  public void runPairExisting() throws Exception {
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ CLI INITIALIZATION - PAIR EXISTING SAMOURAIWALLET");
    log.info("⣿ This will intialize CLI by connecting an existing Samourai Wallet.");
    log.info("⣿ ");

    // pairing payload
    WhirlpoolPairingPayload pairing = readParingPayload();
    doInit(pairing);
  }

  public void runCreateWallet() throws Exception {
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ CLI INITIALIZATION - CREATE WALLET");
    log.info("⣿ This will initialize CLI by creating a new Samourai Wallet.");
    log.info("⣿ ");

    // passphrase
    String passphrase = readPassphraseCreate();

    // networkParams
    NetworkParameters params = readNetworkParams();

    // dojo settings
    PairingDojo pairingDojo = readPairingDojo();

    // create wallet
    Pair<WhirlpoolPairingPayload, String> pair =
        cliConfigService.createWallet(pairingDojo, passphrase, params);
    WhirlpoolPairingPayload pairing = pair.getFirst();
    String mnemonic = pair.getSecond();

    // confirm mnemonic
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ WALLET MNEMONIC CREATED");
    log.info("⣿ Your mnemonic is: " + mnemonic);
    log.info("⣿ • Save it in a safe place, and re-type the last word:");
    String[] words = mnemonic.split(" ");
    String lastWord = words[words.length - 1];
    CliUtils.readUserInputRequired("Last mnemonic word?", true, new String[] {lastWord});

    doInit(pairing);
  }

  public void runRestoreExternal() throws Exception {
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ CLI INITIALIZATION - RESTORE EXTERNAL WALLET");
    log.info("⣿ This will initialize CLI by restoring an external wallet.");
    log.info("⣿ ");

    // networkParams
    NetworkParameters params = readNetworkParams();

    // mnemonic
    String mnemonic = readMnemonic(params);

    // passphrase
    boolean appendPassphrase = readAppendPassphrase();
    String passphrase = readPassphraseRestore(appendPassphrase);

    // dojo settings
    PairingDojo pairingDojo = readPairingDojo();

    // restore wallet
    WhirlpoolPairingPayload pairing =
        cliConfigService.restoreExternal(
            pairingDojo, passphrase, params, mnemonic, appendPassphrase);

    // init
    doInit(pairing);
  }

  public void runRestoreBackup() throws Exception {
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ CLI INITIALIZATION - RESTORE SAMOURAIWALLET BACKUP");
    log.info("⣿ This will initialize CLI by restoring a Samourai Wallet backup.");
    log.info("⣿ ");

    // read from restore payload
    WhirlpoolPairingPayload pairing = readRestoreBackup();

    // init
    doInit(pairing);
  }

  public void doInit(WhirlpoolPairingPayload pairing) throws Exception {
    // Tor
    boolean tor = readTor(pairing.getDojo() != null);

    // init
    String apiKey = cliConfigService.initialize(pairing, tor, null);

    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ INITIALIZATION SUCCESS");
    log.info("⣿ Take note of your API Key, to connect remotely from GUI or API.");
    log.info("⣿ Your API key is: " + apiKey);
    log.info("⣿ ");
    log.info("⣿ Restarting CLI...");
    log.info(CliUtils.LOG_SEPARATOR);
  }

  private WhirlpoolPairingPayload readParingPayload() throws Exception {
    log.info(
        "⣿ Get your pairing payload in Samourai Wallet, go to 'Settings/Transactions/Experimental'");
    log.info("⣿ • Paste your pairing payload:");
    String pairingPayload = CliUtils.readUserInputRequired("Pairing payload?", false);
    log.info("⣿ ");
    return cliConfigService.parsePairingPayload(pairingPayload);
  }

  private String readMnemonic(NetworkParameters params) throws Exception {
    do {
      log.info("⣿ Enter your wallet seed (12, 15, 18 or 24 words).");
      log.info("⣿ • Type SEED WORDS:");
      String words = CliUtils.readUserInputRequired("Mnemonic words?", false);

      try {
        // validate mnemonic
        HD_WalletFactoryGeneric.getInstance().restoreWalletFromWords(words, null, params);
        return words;
      } catch (Exception e) {
        log.error("⣿ ERROR - invalid seed words.");
      }
    } while (true);
  }

  private boolean readAppendPassphrase() throws Exception {
    log.info("⣿ Use a BIP-39 passphrase for your wallet?");
    return CliUtils.readUserInputRequiredBoolean("Use a BIP-39 passphrase?");
  }

  private String readPassphraseCreate() throws Exception {
    return doReadPassphrase(
        "Choose a BIP-39 passphrase for your wallet. It will be the last word of your seed.");
  }

  private String readPassphraseRestore(boolean appendPassphrase) throws Exception {
    String info;
    if (appendPassphrase) {
      info = "Type the BIP-39 passphrase of your wallet. It will be the last word of your seed.";
    } else {
      info = "Type a passphrase to protect your Samourai Wallet. It WON'T be added to your seed.";
    }
    return doReadPassphrase(info);
  }

  private String doReadPassphrase(String info) throws Exception {
    log.info("⣿ " + info);
    log.info("⣿ • Type a PASSPHRASE:");
    String passphrase =
        CliUtils.readUserInputRequiredMinLength("Passphrase?", true, PASSPHRASE_MINLENGTH);

    log.info("⣿ • Confirm PASSPHRASE:");
    CliUtils.readUserInputRequired("Passphrase?", true, new String[] {passphrase});
    return passphrase;
  }

  private NetworkParameters readNetworkParams() {
    log.info("⣿ • Use Testnet network?");
    boolean isTestnet = CliUtils.readUserInputRequiredBoolean("Use testnet?");
    return FormatsUtilGeneric.getInstance().getNetworkParams(isTestnet);
  }

  private PairingDojo readPairingDojo() throws Exception {
    // use Dojo?
    log.info("⣿ • Use your own Dojo server?");
    boolean useDojo = CliUtils.readUserInputRequiredBoolean("Use Dojo?");
    PairingDojo dojo = null;
    if (useDojo) {
      String dojoUrl = CliUtils.readUserInputRequired("Dojo url?", false);
      String dojoApiKey = CliUtils.readUserInputRequired("Dojo apiKey?", false);
      dojo = cliConfigService.computePairingDojo(dojoUrl, dojoApiKey);
    }
    return dojo;
  }

  private PairingDojo readPairingDojo(PairingDojo existingDojo) throws Exception {
    if (existingDojo != null) {
      // use existing Dojo?
      log.info("⣿ • Use existing own Dojo server (" + existingDojo.getUrl() + ")?");
      boolean useDojo = CliUtils.readUserInputRequiredBoolean("Use Dojo?");
      if (useDojo) {
        return existingDojo;
      }
    }
    // use another Dojo?
    return readPairingDojo();
  }

  private WhirlpoolPairingPayload readRestoreBackup() throws Exception {
    // backup payload
    String encryptedBackup = null;
    while (encryptedBackup == null) {
      log.info("⣿ • Enter the path to your Samourai Wallet backup file:");
      File f = CliUtils.readFileName("Backup file?");
      log.info("⣿ ");
      encryptedBackup = FileUtil.readAsString(f);

      // validate
      if (!PayloadUtilGeneric.getInstance().isBackupFile(encryptedBackup)) {
        log.error("⣿ ERROR - invalid backup payload.");
        encryptedBackup = null;
      }
    }

    // passphrase
    String passphrase = null;
    BackupPayload backup = null;
    while (backup == null) {
      try {
        log.info("⣿ • Type your PASSPHRASE:");
        passphrase =
            CliUtils.readUserInputRequiredMinLength("Passphrase?", true, PASSPHRASE_MINLENGTH);
        backup = PayloadUtilGeneric.getInstance().readBackup(encryptedBackup, passphrase);
      } catch (Exception e) {
        log.error("⣿ ERROR - invalid PASSPHRASE for this backup.");
      }
    }

    // dojo settings
    PairingDojo pairingDojo = readPairingDojo(backup.computePairingDojo());

    // restore wallet
    WhirlpoolPairingPayload pairing =
        cliConfigService.restoreBackup(backup, pairingDojo, passphrase);
    return pairing;
  }

  private boolean readTor(boolean dojo) {
    boolean tor;
    if (dojo) {
      // dojo => Tor enabled
      log.info("⣿ Enabling Tor (required for Dojo).");
      tor = true;
    } else {
      // samourai backend => Tor optional
      log.info("⣿ • Enable Tor? (you can change this later)");
      tor = CliUtils.readUserInputRequiredBoolean("Enable Tor?");
      log.info("⣿ ");
    }
    return tor;
  }
}
