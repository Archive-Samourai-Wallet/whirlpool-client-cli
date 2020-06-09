package com.samourai.whirlpool.cli.services;

import com.google.common.primitives.Bytes;
import com.samourai.http.client.HttpUsage;
import com.samourai.http.client.JavaHttpClient;
import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.wallet.api.pairing.PairingNetwork;
import com.samourai.wallet.api.pairing.PairingPayload;
import com.samourai.wallet.api.pairing.PairingVersion;
import com.samourai.wallet.crypto.AESUtil;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.java.HD_WalletFactoryJava;
import com.samourai.wallet.util.CharSequenceX;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.wallet.util.oauth.OAuthManager;
import com.samourai.wallet.util.oauth.OAuthManagerJava;
import com.samourai.whirlpool.cli.beans.CliState;
import com.samourai.whirlpool.cli.beans.CliStatus;
import com.samourai.whirlpool.cli.beans.WhirlpoolPairingPayload;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.config.CliConfigFile;
import com.samourai.whirlpool.cli.exception.AuthenticationException;
import com.samourai.whirlpool.cli.exception.NoSessionWalletException;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.wallet.WhirlpoolDataService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletConfig;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletService;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java8.util.Optional;
import javax.crypto.AEADBadTagException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.MnemonicException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CliWalletService extends WhirlpoolWalletService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final FormatsUtilGeneric formatUtils = FormatsUtilGeneric.getInstance();

  private CliConfig cliConfig;
  private CliConfigService cliConfigService;
  private HD_WalletFactoryJava hdWalletFactory;
  private WalletAggregateService walletAggregateService;
  private JavaHttpClientService httpClientService;
  private JavaStompClientService stompClientService;
  private CliTorClientService cliTorClientService;

  public CliWalletService(
      CliConfig cliConfig,
      CliConfigService cliConfigService,
      HD_WalletFactoryJava hdWalletFactory,
      WalletAggregateService walletAggregateService,
      JavaHttpClientService httpClientService,
      JavaStompClientService stompClientService,
      CliTorClientService cliTorClientService) {
    super();
    this.cliConfig = cliConfig;
    this.cliConfigService = cliConfigService;
    this.hdWalletFactory = hdWalletFactory;
    this.walletAggregateService = walletAggregateService;
    this.httpClientService = httpClientService;
    this.stompClientService = stompClientService;
    this.cliTorClientService = cliTorClientService;
  }

  public CliWallet openWallet(String passphrase) throws Exception {
    // require CliStatus.READY
    if (!CliStatus.READY.equals(cliConfigService.getCliStatus())) {
      throw new NotifiableException(
          "Cannot start wallet: cliStatus=" + cliConfigService.getCliStatus());
    }

    NetworkParameters params = cliConfig.getServer().getParams();

    // decrypt seed
    String seedWords;
    try {
      seedWords = decryptSeedWords(cliConfig.getSeed(), passphrase);
    } catch (Exception e) {
      if (log.isDebugEnabled()
          && !(e instanceof AEADBadTagException)
          && !(e instanceof InvalidCipherTextException)) {
        log.debug("", e);
      }
      throw new AuthenticationException("Authentication failed: invalid passphrase?");
    }

    String walletIdentifier;
    HD_Wallet bip84w;
    try {
      // init wallet from seed
      byte[] seed = hdWalletFactory.computeSeedFromWords(seedWords);
      String walletPassphrase = cliConfig.isSeedAppendPassphrase() ? passphrase : "";
      bip84w = hdWalletFactory.getBIP84(seed, walletPassphrase, params);

      // identifier
      walletIdentifier = computeWalletIdentifier(seed, passphrase, params);
    } catch (MnemonicException e) {
      throw new NotifiableException("Mnemonic failed: invalid passphrase?");
    }

    // debug cliConfig
    if (log.isDebugEnabled()) {
      log.debug("openWallet with cliConfig:");
      for (Map.Entry<String, String> entry : cliConfig.getConfigInfo().entrySet()) {
        log.debug("[cliConfig/" + entry.getKey() + "] " + entry.getValue());
      }
    }

    // backend connexion
    BackendApi backendApiService = computeBackendApiService(passphrase);
    if (!backendApiService.testConnectivity()) {
      throw new NotifiableException(
          "Unable to connect to wallet backend: " + backendApiService.getUrlBackend());
    }

    // open wallet
    WhirlpoolWalletConfig config =
        cliConfig.computeWhirlpoolWalletConfig(
            httpClientService, stompClientService, backendApiService);

    String walletStateFileName = computeIndexFile(walletIdentifier).getAbsolutePath();
    String utxoConfigFileName = computeUtxosFile(walletIdentifier).getAbsolutePath();
    WhirlpoolDataService dataService = new WhirlpoolDataService(config);

    WhirlpoolWallet whirlpoolWallet =
        computeWhirlpoolWallet(dataService, bip84w, walletStateFileName, utxoConfigFileName);
    CliWallet cliWallet =
        new CliWallet(
            whirlpoolWallet,
            cliConfig,
            cliConfigService,
            walletAggregateService,
            cliTorClientService,
            httpClientService);
    return (CliWallet) openWallet(cliWallet);
  }

  private BackendApi computeBackendApiService(String passphrase) throws Exception {
    String backendUrl = cliConfig.computeBackendUrl();

    // dojo OAuth
    Optional<OAuthManager> oAuthManager = Optional.empty();
    String dojoApiKeyEncrypted = cliConfig.computeBackendApiKey();
    if (dojoApiKeyEncrypted != null) {
      String dojoApiKey = decryptApiKey(dojoApiKeyEncrypted, passphrase);
      oAuthManager = Optional.of(new OAuthManagerJava(dojoApiKey));
    }
    JavaHttpClient httpClient = httpClientService.getHttpClient(HttpUsage.BACKEND);
    return new BackendApi(httpClient, backendUrl, oAuthManager);
  }

  protected String decryptSeedWords(String seedWordsEncrypted, String seedPassphrase)
      throws Exception {
    return AESUtil.decrypt(seedWordsEncrypted, new CharSequenceX(seedPassphrase));
  }

  protected String decryptApiKey(String apiKey, String seedPassphrase) throws Exception {
    return AESUtil.decrypt(apiKey, new CharSequenceX(seedPassphrase));
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

  private String computeWalletIdentifier(
      byte[] seed, String seedPassphrase, NetworkParameters params) {
    return ClientUtils.sha256Hash(
        Bytes.concat(seed, seedPassphrase.getBytes(), params.getId().getBytes()));
  }

  private File computeIndexFile(String walletIdentifier) throws NotifiableException {
    String path = "whirlpool-cli-state-" + walletIdentifier + ".json";
    return CliUtils.computeFile(path);
  }

  private File computeUtxosFile(String walletIdentifier) throws NotifiableException {
    String path = "whirlpool-cli-utxos-" + walletIdentifier + ".json";
    return CliUtils.computeFile(path);
  }

  public CliState getCliState() {
    CliStatus cliStatus = cliConfigService.getCliStatus();
    String cliMessage = cliConfigService.getCliMessage();
    boolean loggedIn = hasSessionWallet();

    Integer torProgress = cliTorClientService.getProgress().orElse(null);
    return new CliState(cliStatus, cliMessage, loggedIn, torProgress);
  }

  public String computePairingPayload() {
    PairingNetwork pairingNetwork =
        formatUtils.isTestNet(cliConfig.getServer().getParams())
            ? PairingNetwork.TESTNET
            : PairingNetwork.MAINNET;

    PairingPayload.PairingDojo dojo = null;
    CliConfigFile.DojoConfig dojoConfig = cliConfig.getDojo();
    if (dojoConfig.isEnabled()) {
      dojo = new PairingPayload.PairingDojo(dojoConfig.getUrl(), dojoConfig.getApiKey());
    }

    PairingPayload pairingPayload =
        new WhirlpoolPairingPayload(
            PairingVersion.V3_0_0,
            pairingNetwork,
            cliConfig.getSeed(),
            cliConfig.isSeedAppendPassphrase(),
            dojo);
    String json = ClientUtils.toJsonString(pairingPayload);
    return json;
  }
}
