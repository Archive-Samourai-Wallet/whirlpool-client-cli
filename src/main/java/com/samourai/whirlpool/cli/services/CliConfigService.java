package com.samourai.whirlpool.cli.services;

import com.samourai.wallet.api.pairing.PairingDojo;
import com.samourai.wallet.api.pairing.PairingNetwork;
import com.samourai.wallet.api.pairing.PairingPayload;
import com.samourai.wallet.crypto.AESUtil;
import com.samourai.wallet.util.CharSequenceX;
import com.samourai.wallet.util.SystemUtil;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiCliConfig;
import com.samourai.whirlpool.cli.beans.CliStatus;
import com.samourai.whirlpool.cli.beans.WhirlpoolPairingPayload;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.cli.utils.SortedProperties;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolServer;
import io.reactivex.functions.Consumer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DefaultPropertiesPersister;

@Service
public class CliConfigService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final String CLI_CONFIG_FILENAME = "whirlpool-cli-config.properties";
  private static final String KEY_APIKEY = "cli.apiKey";
  private static final String KEY_SEED = "cli.seed";
  private static final String KEY_SEED_APPEND_PASSPHRASE = "cli.seedAppendPassphrase";
  private static final String KEY_DOJO_URL = "cli.dojo.url";
  private static final String KEY_DOJO_APIKEY = "cli.dojo.apiKey";
  public static final String KEY_DOJO_ENABLED = "cli.dojo.enabled";
  private static final String KEY_VERSION = "cli.version";
  public static final String KEY_MIX_CLIENTS = "cli.mix.clients";
  private static final String KEY_EXTERNAL_DESTINATION_XPUB = "cli.externalDestination.xpub";
  private static final String KEY_EXTERNAL_DESTINATION_CHAIN = "cli.externalDestination.chain";
  private static final String KEY_EXTERNAL_DESTINATION_START_INDEX =
      "cli.externalDestination.startIndex";
  private static final String KEY_EXTERNAL_DESTINATION_MIXS = "cli.externalDestination.mixs";
  public static final String KEY_EXTERNAL_DESTINATION_DISABLED = "cli.externalDestination.disabled";

  private CliConfig cliConfig;
  private CliStatus cliStatus;
  private String cliMessage;

  public CliConfigService(CliConfig cliConfig) {
    this.cliConfig = cliConfig;
    this.cliStatus = CliStatus.STARTING;
    if (Strings.isEmpty(cliConfig.getSeed()) || Strings.isEmpty(cliConfig.getApiKey())) {
      this.setCliStatus(CliStatus.NOT_INITIALIZED);
    }
  }

  public CliStatus getCliStatus() {
    return cliStatus;
  }

  public String getCliMessage() {
    return cliMessage;
  }

  protected void setCliStatus(CliStatus cliStatus) {
    this.setCliStatus(cliStatus, null);
  }

  protected void setCliStatus(CliStatus cliStatus, String cliMessage) {
    this.cliStatus = cliStatus;
    this.cliMessage = cliMessage;
  }

  public boolean isCliStatusReady() {
    return CliStatus.READY.equals(cliStatus);
  }

  public boolean isCliStatusNotInitialized() {
    return CliStatus.NOT_INITIALIZED.equals(cliStatus);
  }

  public WhirlpoolPairingPayload parsePairingPayload(String pairingWalletPayload) throws Exception {
    return WhirlpoolPairingPayload.parse(pairingWalletPayload);
  }

  protected String encryptDojoApiKey(String dojoApiKey, String seedPassphrase) throws Exception {
    String encrypted = AESUtil.encrypt(dojoApiKey, new CharSequenceX(seedPassphrase));
    return encrypted;
  }

  public PairingDojo computePairingDojo(String dojoUrl, String dojoApiKey) throws Exception {
    String dojoApiKeyEncrypted = encryptDojoApiKey(dojoUrl, dojoApiKey);
    return new PairingDojo(dojoUrl, dojoApiKeyEncrypted);
  }

  public String initialize(WhirlpoolPairingPayload pairingWallet, boolean tor, Boolean dojo)
      throws Exception {
    // parse payload

    // use dojo?
    String dojoUrl = null;
    String dojoApiKeyEncrypted = null;
    PairingDojo pairingDojo = pairingWallet.getDojo();
    if (pairingDojo != null) {
      dojoUrl = pairingDojo.getUrl();
      dojoApiKeyEncrypted = pairingDojo.getApikey();
      if (dojo == null) {
        dojo = true;
      }
    } else {
      if (dojo == null) {
        dojo = false;
      }
      if (dojo) {
        throw new NotifiableException("Cannot enable DOJO: dojo pairing not found");
      }
    }

    // initialize
    PairingPayload.PairingValue pairing = pairingWallet.getPairing();
    String encryptedMnemonic = pairing.getMnemonic();
    boolean appendPassphrase = pairing.getPassphrase();
    PairingNetwork pairingNetwork = pairing.getNetwork();
    WhirlpoolServer whirlpoolServer =
        PairingNetwork.MAINNET.equals(pairingNetwork)
            ? WhirlpoolServer.MAINNET
            : WhirlpoolServer.TESTNET;

    return initialize(
        encryptedMnemonic,
        appendPassphrase,
        whirlpoolServer,
        tor,
        dojoUrl,
        dojoApiKeyEncrypted,
        dojo);
  }

  public synchronized String initialize(
      String encryptedMnemonic,
      boolean appendPassphrase,
      WhirlpoolServer whirlpoolServer,
      boolean tor,
      String dojoUrl,
      String dojoApiKeyEncrypted,
      boolean dojoEnabled)
      throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(" • initialize");
    }
    if (whirlpoolServer == null) {
      throw new NotifiableException("Invalid server");
    }
    if (StringUtils.isEmpty(encryptedMnemonic)) {
      throw new NotifiableException("Invalid mnemonic");
    }

    // preserve apiKey when already defined
    String apiKey = cliConfig.getApiKey();
    if (StringUtils.isEmpty(apiKey)) {
      // generate apiKey when missing
      apiKey = CliUtils.generateUniqueString();
    }

    // save configuration file
    Properties props = new Properties();
    props.put(KEY_VERSION, Integer.toString(CliUpgradeService.CURRENT_VERSION.getVersion()));
    props.put(KEY_APIKEY, apiKey);
    props.put(KEY_SEED, encryptedMnemonic);
    props.put(KEY_SEED_APPEND_PASSPHRASE, Boolean.toString(appendPassphrase));
    props.put(ApiCliConfig.KEY_SERVER, whirlpoolServer.name());
    props.put(ApiCliConfig.KEY_TOR, Boolean.toString(tor));
    if (dojoUrl != null) {
      props.put(KEY_DOJO_URL, dojoUrl);
    }
    if (dojoApiKeyEncrypted != null) {
      props.put(KEY_DOJO_APIKEY, dojoApiKeyEncrypted);
    }
    props.put(KEY_DOJO_ENABLED, Boolean.toString(dojoEnabled));
    try {
      saveProperties(props);
    } catch (Exception e) {
      log.error("", e);
      throw new NotifiableException("Unable to save CLI configuration");
    }

    // restart needed
    this.setCliStatusNotReady("Wallet initialization success. Restarting CLI...");
    return apiKey;
  }

  public Properties loadProperties() throws Exception {
    Resource resource = new FileSystemResource(getConfigurationFile());
    Properties props = PropertiesLoaderUtils.loadProperties(resource);
    return props;
  }

  public synchronized void setApiConfig(ApiCliConfig apiCliConfig) throws Exception {
    if (apiCliConfig.getDojo() && !apiCliConfig.getTor()) {
      throw new NotifiableException("Tor is required for DOJO");
    }

    Properties props = loadProperties();

    apiCliConfig.toProperties(props);

    // save
    saveProperties(props);

    // restart needed
    this.setCliStatusNotReady("ApiConfig updated. Restarting CLI...");
  }

  public synchronized void setExternalDestination(
      String xpub, int chain, int startIndex, int mixs, String passphrase) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(" • setExternalDestination");
    }
    if (StringUtils.isEmpty(xpub)) {
      throw new NotifiableException("Invalid externalDestination.xpub");
    }
    // encrypt xpub
    String xpubEncrypted = AESUtil.encrypt(xpub, new CharSequenceX(passphrase));
    if (StringUtils.isEmpty(xpubEncrypted)) {
      throw new NotifiableException("Invalid externalDestination.xpub");
    }
    if (chain < 0) {
      throw new NotifiableException("Invalid externalDestination.chain");
    }
    if (startIndex < 0) {
      throw new NotifiableException("Invalid externalDestination.startIndex");
    }
    if (mixs < 1) {
      throw new NotifiableException("Invalid externalDestination.mixs");
    }

    // set
    Properties props = loadProperties();
    props.put(KEY_EXTERNAL_DESTINATION_XPUB, xpubEncrypted);
    props.put(KEY_EXTERNAL_DESTINATION_CHAIN, Integer.toString(chain));
    props.put(KEY_EXTERNAL_DESTINATION_START_INDEX, Integer.toString(startIndex));
    props.put(KEY_EXTERNAL_DESTINATION_MIXS, Integer.toString(mixs));
    props.put(KEY_EXTERNAL_DESTINATION_DISABLED, Boolean.toString(false));

    // save
    saveProperties(props);

    // restart needed
    this.setCliStatusNotReady("ExternalDestination updated, CLI restart required.");
  }

  public synchronized void setExternalDestinationDisabled(boolean disabled) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(" • setExternalDestinationDisabled");
    }

    // set
    Properties props = loadProperties();
    props.put(KEY_EXTERNAL_DESTINATION_DISABLED, Boolean.toString(disabled));

    // save
    saveProperties(props);

    // restart needed
    this.setCliStatusNotReady("ExternalDestination updated, CLI restart required.");
  }

  public synchronized void clearExternalDestination() throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(" • clearExternalDestination");
    }

    // unset
    Properties props = loadProperties();
    props.remove(KEY_EXTERNAL_DESTINATION_XPUB);
    props.remove(KEY_EXTERNAL_DESTINATION_CHAIN);
    props.remove(KEY_EXTERNAL_DESTINATION_START_INDEX);
    props.remove(KEY_EXTERNAL_DESTINATION_MIXS);
    props.remove(KEY_EXTERNAL_DESTINATION_DISABLED);

    // save
    saveProperties(props);

    // restart needed
    this.setCliStatusNotReady("ExternalDestination updated, CLI restart required.");
  }

  public synchronized void setVersion(int version) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("setVersion: " + version);
    }
    Properties props = loadProperties();
    props.put(KEY_VERSION, Integer.toString(version));

    // save
    saveProperties(props);

    // update runtime
    cliConfig.setVersion(version);
  }

  public synchronized void resetConfiguration() throws Exception {
    log.info("resetConfiguration");

    File f = getConfigurationFile();
    if (f.exists()) {
      if (!f.delete()) {
        throw new NotifiableException("Could not delete " + f.getAbsolutePath());
      }
    }

    // restart needed
    this.setCliStatusNotReady("Configuration reset. Restarting CLI...");
  }

  public void setCliStatusNotReady(String error) {
    this.setCliStatus(CliStatus.NOT_READY, error);
    log.warn("status -> " + error);
  }

  public synchronized void saveProperties(Properties unsortedProps) throws Exception {
    if (unsortedProps.isEmpty()) {
      throw new IllegalArgumentException("Configuration to save is empty");
    }

    // sort
    final Properties props = new SortedProperties(unsortedProps);

    // log
    if (log.isDebugEnabled()) {
      for (Entry<Object, Object> entry : props.entrySet()) {
        log.debug(
            "set "
                + entry.getKey()
                + ": "
                + ClientUtils.maskString(String.valueOf(entry.getValue())));
      }
    }

    // write
    File f = getConfigurationFile();
    Consumer<File> callback =
        tempFile -> {
          OutputStream out = new FileOutputStream(tempFile);
          try {
            DefaultPropertiesPersister p = new DefaultPropertiesPersister();
            p.store(props, out, "Updated by application");
          } finally {
            out.close();
          }
        };
    SystemUtil.safeWrite(f, callback);
  }

  private File getConfigurationFile() {
    return new File(CLI_CONFIG_FILENAME);
  }
}
