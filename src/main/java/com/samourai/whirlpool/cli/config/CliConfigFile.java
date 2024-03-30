package com.samourai.whirlpool.cli.config;

import com.samourai.soroban.client.SorobanConfig;
import com.samourai.wallet.constants.SamouraiNetwork;
import com.samourai.wallet.crypto.AESUtil;
import com.samourai.wallet.httpClient.HttpProxy;
import com.samourai.wallet.util.CharSequenceX;
import com.samourai.whirlpool.cli.beans.CliTorExecutableMode;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletConfig;
import com.samourai.whirlpool.client.wallet.beans.ExternalDestination;
import com.samourai.whirlpool.client.wallet.data.dataSource.DataSourceFactory;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "cli")
@Configuration
public abstract class CliConfigFile {
  private int version; // 0 for versions < 1
  private SamouraiNetwork server; // not renamed for backward compatibility
  private String scode;
  private String partner;
  @NotEmpty private boolean tor;
  @NotEmpty private TorConfig torConfig;
  @NotEmpty private DojoConfig dojo;
  @NotEmpty private String apiKey;
  @NotEmpty private String seed;
  @NotEmpty private boolean seedAppendPassphrase;
  @NotEmpty private int tx0MinConfirmations;
  @NotEmpty private String proxy;

  @Min(value = 1000)
  private long requestTimeout;

  private Optional<HttpProxy> _cliProxy;
  @NotEmpty private MixConfig mix;
  @NotEmpty private ApiConfig api;
  @NotEmpty private ExternalDestinationConfig externalDestination;

  @Autowired BuildProperties buildProperties;

  public CliConfigFile() {
    // warning: properties are NOT loaded yet
    // it will be loaded later on SpringBoot application run()
    this.mix = new MixConfig();
    this.api = new ApiConfig();
    this.externalDestination = new ExternalDestinationConfig();
  }

  public CliConfigFile(CliConfigFile copy) {
    this.version = copy.version;
    this.server = copy.server;
    this.scode = copy.scode;
    this.partner = copy.partner;
    this.tor = copy.tor;
    this.torConfig = new TorConfig(copy.torConfig);
    this.dojo = new DojoConfig(copy.dojo);
    this.apiKey = copy.apiKey;
    this.seed = copy.seed;
    this.seedAppendPassphrase = copy.seedAppendPassphrase;
    this.tx0MinConfirmations = copy.tx0MinConfirmations;
    this.proxy = copy.proxy;
    this.requestTimeout = copy.requestTimeout;
    this.api = new ApiConfig(copy.api);
    this.externalDestination = new ExternalDestinationConfig(copy.externalDestination);
    this.mix = new MixConfig(copy.mix);
    this.buildProperties = copy.buildProperties;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  @Deprecated
  public SamouraiNetwork getServer() {
    return server;
  }

  @Deprecated
  public void setServer(SamouraiNetwork server) {
    this.server = server;
  }

  public String getScode() {
    return scode;
  }

  public void setScode(String scode) {
    this.scode = scode;
  }

  public String getPartner() {
    return partner;
  }

  public void setPartner(String partner) {
    this.partner = partner;
  }

  public boolean getTor() {
    return tor;
  }

  public void setTor(boolean tor) {
    this.tor = tor;
  }

  public TorConfig getTorConfig() {
    return torConfig;
  }

  public void setTorConfig(TorConfig torConfig) {
    this.torConfig = torConfig;
  }

  public DojoConfig getDojo() {
    return dojo;
  }

  public void setDojo(DojoConfig dojo) {
    this.dojo = dojo;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public ExternalDestinationConfig getExternalDestination() {
    return externalDestination;
  }

  public boolean isExternalDestinationConfigured() {
    return externalDestination != null && !StringUtils.isEmpty(externalDestination.getXpub());
  }

  public boolean isExternalDestinationEnabled() {
    return isExternalDestinationConfigured() && !externalDestination.isDisabled();
  }

  public void setExternalDestination(ExternalDestinationConfig externalDestination) {
    this.externalDestination = externalDestination;
  }

  public String getSeed() {
    return seed;
  }

  public void setSeed(String seed) {
    this.seed = seed;
  }

  public boolean isSeedAppendPassphrase() {
    return seedAppendPassphrase;
  }

  public void setSeedAppendPassphrase(boolean seedAppendPassphrase) {
    this.seedAppendPassphrase = seedAppendPassphrase;
  }

  public int getTx0MinConfirmations() {
    return tx0MinConfirmations;
  }

  public void setTx0MinConfirmations(int tx0MinConfirmations) {
    this.tx0MinConfirmations = tx0MinConfirmations;
  }

  public String getProxy() {
    return proxy;
  }

  public Optional<HttpProxy> getCliProxy() {
    if (_cliProxy == null) {
      _cliProxy = CliUtils.computeProxy(proxy);
    }
    return _cliProxy;
  }

  public void setProxy(String proxy) {
    this.proxy = proxy;
  }

  public long getRequestTimeout() {
    return requestTimeout;
  }

  public void setRequestTimeout(long requestTimeout) {
    this.requestTimeout = requestTimeout;
  }

  public MixConfig getMix() {
    return mix;
  }

  public void setMix(MixConfig mix) {
    this.mix = mix;
  }

  public ApiConfig getApi() {
    return api;
  }

  public void setApi(ApiConfig api) {
    this.api = api;
  }

  public static class MixConfig {
    @NotEmpty private int clients;
    @NotEmpty private int clientsPerPool;
    @NotEmpty private int extraLiquidityClientsPerPool;
    @NotEmpty private int clientDelay;
    @NotEmpty private int autoTx0Delay;
    @NotEmpty private int tx0MaxOutputs;
    @NotEmpty private boolean autoMix;
    private Map<String, Long> overspend;
    @NotEmpty private boolean testMode;

    public MixConfig() {}

    public MixConfig(MixConfig copy) {
      this.clients = copy.clients;
      this.clientsPerPool = copy.clientsPerPool;
      this.extraLiquidityClientsPerPool = copy.extraLiquidityClientsPerPool;
      this.clientDelay = copy.clientDelay;
      this.autoTx0Delay = copy.autoTx0Delay;
      this.tx0MaxOutputs = copy.tx0MaxOutputs;
      this.autoMix = copy.autoMix;
      this.overspend = copy.overspend != null ? new HashMap<>(copy.overspend) : null;
      this.testMode = copy.testMode;
    }

    public int getClients() {
      return clients;
    }

    // public void setClients(int clients) {
    public void setClients(Integer clients) {
      this.clients = clients;
    }

    public int getClientsPerPool() {
      return clientsPerPool;
    }

    public void setClientsPerPool(int clientsPerPool) {
      this.clientsPerPool = clientsPerPool;
    }

    public int getExtraLiquidityClientsPerPool() {
      return extraLiquidityClientsPerPool;
    }

    public void setExtraLiquidityClientsPerPool(int extraLiquidityClientsPerPool) {
      this.extraLiquidityClientsPerPool = extraLiquidityClientsPerPool;
    }

    public int getClientDelay() {
      return clientDelay;
    }

    public void setClientDelay(int clientDelay) {
      this.clientDelay = clientDelay;
    }

    public int getAutoTx0Delay() {
      return autoTx0Delay;
    }

    public void setAutoTx0Delay(int autoTx0Delay) {
      this.autoTx0Delay = autoTx0Delay;
    }

    public int getTx0MaxOutputs() {
      return tx0MaxOutputs;
    }

    public void setTx0MaxOutputs(int tx0MaxOutputs) {
      this.tx0MaxOutputs = tx0MaxOutputs;
    }

    public boolean isAutoMix() {
      return autoMix;
    }

    public void setAutoMix(boolean autoMix) {
      this.autoMix = autoMix;
    }

    public Map<String, Long> getOverspend() {
      return overspend;
    }

    public void setOverspend(Map<String, Long> overspend) {
      this.overspend = overspend;
    }

    public boolean isTestMode() {
      return testMode;
    }

    public void setTestMode(boolean testMode) {
      this.testMode = testMode;
    }

    public Map<String, String> getConfigInfo() {
      Map<String, String> configInfo = new HashMap<>();
      configInfo.put("cli/mix/clients", Integer.toString(clients));
      configInfo.put("cli/mix/clientsPerPool", Integer.toString(clientsPerPool));
      configInfo.put(
          "cli/mix/extraLiquidityClientsPerPool", Integer.toString(extraLiquidityClientsPerPool));
      configInfo.put("cli/mix/clientDelay", Integer.toString(clientDelay));
      configInfo.put("cli/mix/autoTx0Delay", Integer.toString(autoTx0Delay));
      configInfo.put("cli/mix/tx0MaxOutputs", Integer.toString(tx0MaxOutputs));
      configInfo.put("cli/mix/autoMix", Boolean.toString(autoMix));
      configInfo.put("cli/mix/overspend", overspend != null ? overspend.toString() : "null");
      configInfo.put("cli/mix/testMode", Boolean.toString(testMode));
      return configInfo;
    }
  }

  public static class ApiConfig {
    @NotEmpty private int port;
    @NotEmpty private int httpPort;
    @NotEmpty private boolean httpEnable;

    public ApiConfig() {}

    public ApiConfig(ApiConfig copy) {
      this.port = copy.port;
      this.httpPort = copy.httpPort;
      this.httpEnable = copy.httpEnable;
    }

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public int getHttpPort() {
      return httpPort;
    }

    public void setHttpPort(int httpPort) {
      this.httpPort = httpPort;
    }

    public boolean isHttpEnable() {
      return httpEnable;
    }

    public void setHttpEnable(boolean httpEnable) {
      this.httpEnable = httpEnable;
    }

    public Map<String, String> getConfigInfo() {
      Map<String, String> configInfo = new HashMap<>();
      String info = "port=" + port;
      if (httpEnable) {
        info += ", httpPort=" + httpPort;
      }
      info += ", httpEnable=" + httpEnable;
      configInfo.put("cli/api", info);
      return configInfo;
    }
  }

  public static class ExternalDestinationConfig {
    @NotEmpty private String xpub;
    @NotEmpty private int chain;
    @NotEmpty private int mixs;
    @NotEmpty private int mixsRandomFactor;
    @NotEmpty private boolean disabled;

    public ExternalDestinationConfig() {}

    public ExternalDestinationConfig(ExternalDestinationConfig copy) {
      this.xpub = copy.xpub;
      this.chain = copy.chain;
      this.mixs = copy.mixs;
      this.mixsRandomFactor = copy.mixsRandomFactor;
      this.disabled = copy.disabled;
    }

    public String getXpub() {
      return xpub;
    }

    public void setXpub(String xpub) {
      this.xpub = xpub;
    }

    public int getChain() {
      return chain;
    }

    public void setChain(int chain) {
      this.chain = chain;
    }

    public int getMixs() {
      return mixs;
    }

    public void setMixs(int mixs) {
      this.mixs = mixs;
    }

    public int getMixsRandomFactor() {
      return mixsRandomFactor;
    }

    public void setMixsRandomFactor(int mixsRandomFactor) {
      this.mixsRandomFactor = mixsRandomFactor;
    }

    public boolean isDisabled() {
      return disabled;
    }

    public void setDisabled(boolean disabled) {
      this.disabled = disabled;
    }

    public Map<String, String> getConfigInfo() {
      Map<String, String> configInfo = new HashMap<>();
      String str =
          (!StringUtils.isEmpty(xpub)
              ? "xpub="
                  + xpub
                  + ", chain="
                  + chain
                  + ", mixs="
                  + mixs
                  + ", mixsRandomFactor="
                  + mixsRandomFactor
                  + ", disabled="
                  + disabled
              : "null");
      configInfo.put("cli/externalDestination", str);
      return configInfo;
    }
  }

  public static class TorConfig {
    @NotEmpty private String executable;
    private CliTorExecutableMode executableMode;
    @NotEmpty private TorConfigItem backend;
    @NotEmpty private TorConfigItem soroban;
    private String customTorrc;
    private int fileCreationTimeout;

    public TorConfig() {}

    public TorConfig(TorConfig copy) {
      this.executable = copy.executable;
      this.backend = copy.backend;
      this.soroban = copy.soroban;
      this.customTorrc = copy.customTorrc;
      this.fileCreationTimeout = copy.fileCreationTimeout;
    }

    public String getExecutable() {
      return executable;
    }

    public void setExecutable(String executable) {
      this.executable = executable;
    }

    public CliTorExecutableMode getExecutableMode() {
      if (executableMode == null) {
        executableMode =
            CliTorExecutableMode.find(this.executable.toUpperCase())
                .orElse(CliTorExecutableMode.SPECIFIED);
      }
      return executableMode;
    }

    public TorConfigItem getBackend() {
      return backend;
    }

    public void setBackend(TorConfigItem backend) {
      this.backend = backend;
    }

    public TorConfigItem getSoroban() {
      return soroban;
    }

    public void setSoroban(TorConfigItem soroban) {
      this.soroban = soroban;
    }

    public String getCustomTorrc() {
      return customTorrc;
    }

    public void setCustomTorrc(String customTorrc) {
      this.customTorrc = customTorrc;
    }

    public int getFileCreationTimeout() {
      return fileCreationTimeout;
    }

    public void setFileCreationTimeout(int fileCreationTimeout) {
      this.fileCreationTimeout = fileCreationTimeout;
    }

    public Map<String, String> getConfigInfo() {
      Map<String, String> configInfo = new HashMap<>();
      configInfo.put("cli/tor/executable", executable);
      configInfo.put("cli/tor/backend", backend.toString());
      configInfo.put("cli/tor/soroban", soroban.toString());
      configInfo.put("cli/tor/customTorrc", customTorrc != null ? customTorrc : "null");
      configInfo.put("cli/tor/fileCreationTimeout", Integer.toString(fileCreationTimeout));
      return configInfo;
    }
  }

  public static class TorConfigItem {
    @NotEmpty private boolean enabled;
    @NotEmpty private boolean onion;

    public TorConfigItem() {}

    public TorConfigItem(TorConfigItem copy) {
      this.enabled = copy.enabled;
      this.onion = copy.onion;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public boolean isOnion() {
      return onion;
    }

    public void setOnion(boolean onion) {
      this.onion = onion;
    }

    public String toString() {
      return "enabled=" + enabled + ", onion=" + onion;
    }
  }

  public static class DojoConfig {
    @NotEmpty private String url;
    @NotEmpty private String apiKey;
    @NotEmpty private boolean enabled;

    public DojoConfig() {}

    public DojoConfig(DojoConfig copy) {
      this.url = copy.url;
      this.apiKey = copy.apiKey;
      this.enabled = copy.enabled;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public Map<String, String> getConfigInfo() {
      Map<String, String> configInfo = new HashMap<>();
      configInfo.put("cli/dojo/url", url);
      configInfo.put("cli/dojo/apiKey", ClientUtils.maskString(apiKey));
      configInfo.put("cli/dojo/enabled", Boolean.toString(enabled));
      return configInfo;
    }
  }

  protected WhirlpoolWalletConfig computeWhirlpoolWalletConfig(
      DataSourceFactory dataSourceFactory, SorobanConfig sorobanConfig, String passphrase)
      throws NotifiableException {

    WhirlpoolWalletConfig config =
        new WhirlpoolWalletConfig(dataSourceFactory, sorobanConfig, false);
    if (!Strings.isEmpty(scode)) {
      config.setScode(scode);
    }
    if (!Strings.isEmpty(partner)) {
      config.setPartner(partner);
    }

    config.setMaxClients(mix.getClients());
    config.setMaxClientsPerPool(mix.getClientsPerPool());
    config.setExtraLiquidityClientsPerPool(mix.getExtraLiquidityClientsPerPool());
    config.setClientDelay(mix.getClientDelay());
    config.setAutoTx0Delay(mix.getAutoTx0Delay());
    config.setTx0MaxOutputs(mix.getTx0MaxOutputs());
    config.setAutoMix(mix.isAutoMix());
    config.setOverspend(mix.getOverspend());

    ExternalDestination ed = computeExternalDestination(passphrase);
    config.setExternalDestination(ed);

    config.setResyncOnFirstRun(true);
    return config;
  }

  private ExternalDestination computeExternalDestination(String passphrase)
      throws NotifiableException {
    if (isExternalDestinationEnabled()
        && this.externalDestination.chain >= 0
        && this.externalDestination.mixs > 0
        && this.externalDestination.mixsRandomFactor >= 0) {
      try {
        // decrypt externalDestination
        String xpubDecrypted =
            AESUtil.decrypt(this.externalDestination.xpub, new CharSequenceX(passphrase));
        return new ExternalDestination(
            xpubDecrypted,
            this.externalDestination.chain,
            this.externalDestination.mixs,
            this.externalDestination.mixsRandomFactor);
      } catch (Exception e) {
        throw new NotifiableException("Invalid config value for: externalDestination.xpub");
      }
    }
    return null;
  }

  public Map<String, String> getConfigInfo() {
    Map<String, String> configInfo = new LinkedHashMap<>();
    configInfo.put("cli/server", server.name());
    configInfo.put("cli/scode", scode);
    configInfo.put("cli/partner", partner != null ? partner : "null");
    configInfo.put("cli/tor", Boolean.toString(tor));
    configInfo.putAll(torConfig.getConfigInfo());
    if (dojo != null) {
      configInfo.putAll(dojo.getConfigInfo());
    } else {
      configInfo.put("cli/dojo", "null");
    }
    configInfo.put("cli/apiKey", ClientUtils.maskString(apiKey));
    configInfo.put("cli/seedEncrypted", ClientUtils.maskString(seed));
    configInfo.put("cli/tx0MinConfirmations", Integer.toString(tx0MinConfirmations));
    configInfo.put("cli/proxy", proxy != null ? ClientUtils.maskString(proxy) : "null");
    configInfo.putAll(mix.getConfigInfo());
    configInfo.putAll(api.getConfigInfo());
    configInfo.putAll(externalDestination.getConfigInfo());
    configInfo.put("cli/buildVersion", getBuildVersion() != null ? getBuildVersion() : "null");
    return configInfo;
  }

  public String getBuildVersion() {
    return buildProperties != null ? buildProperties.getVersion() : null;
  }
}
