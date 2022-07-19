package com.samourai.whirlpool.cli.config;

import com.samourai.http.client.HttpUsage;
import com.samourai.http.client.IHttpClientService;
import com.samourai.stomp.client.IStompClientService;
import com.samourai.tor.client.TorClientService;
import com.samourai.wallet.api.backend.BackendServer;
import com.samourai.wallet.bip47.rpc.secretPoint.ISecretPointFactory;
import com.samourai.wallet.crypto.AESUtil;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.util.CharSequenceX;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.websocket.client.IWebsocketClient;
import com.samourai.websocket.client.JavaWebsocketClient;
import com.samourai.whirlpool.cli.services.JavaHttpClientService;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletConfig;
import com.samourai.whirlpool.client.wallet.data.dataSource.DataSourceFactory;
import com.samourai.whirlpool.client.wallet.data.dataSource.DojoDataSourceFactory;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CliConfig extends CliConfigFile {
  private boolean autoTx0Aggregate;
  private String autoTx0PoolId;
  private boolean resync;

  public CliConfig() {
    super();
  }

  public WhirlpoolWalletConfig computeWhirlpoolWalletConfig(
      ISecretPointFactory secretPointFactory,
      IHttpClientService httpClientService,
      IStompClientService stompClientService,
      TorClientService torClientService,
      String passphrase)
      throws Exception {

    DataSourceFactory dataSourceFactory = computeDataSourceFactory(httpClientService);

    WhirlpoolWalletConfig config =
        super.computeWhirlpoolWalletConfig(
            dataSourceFactory,
            secretPointFactory,
            httpClientService,
            stompClientService,
            torClientService,
            passphrase);
    config.setAutoTx0PoolId(autoTx0PoolId);
    config.setAutoTx0Aggregate(autoTx0Aggregate);
    return config;
  }

  private DataSourceFactory computeDataSourceFactory(IHttpClientService httpClientService)
      throws Exception {
    IWebsocketClient wsClient = new JavaWebsocketClient((JavaHttpClientService) httpClientService);

    // Dojo backend
    if (isDojoEnabled()) {
      String dojoUrl = getDojo().getUrl();
      return new DojoDataSourceFactory(dojoUrl, null, wsClient) {
        @Override
        protected String computeDojoApiKey(
            WhirlpoolWallet whirlpoolWallet, HD_Wallet bip44w, String passphrase) throws Exception {
          return decryptDojoApiKey(getDojo().getApiKey(), passphrase);
        }
      };
    }

    // Samourai backend
    boolean isTestnet = FormatsUtilGeneric.getInstance().isTestNet(getServer().getParams());
    BackendServer backendServer = BackendServer.get(isTestnet);
    boolean useOnion =
        getTor()
            && getTorConfig().getBackend().isEnabled()
            && getTorConfig().getBackend().isOnion();
    return new DojoDataSourceFactory(backendServer, useOnion, wsClient);
  }

  protected static String decryptDojoApiKey(String apiKey, String passphrase) throws Exception {
    return AESUtil.decrypt(apiKey, new CharSequenceX(passphrase));
  }

  public boolean isAutoTx0Aggregate() {
    return autoTx0Aggregate;
  }

  public void setAutoTx0Aggregate(boolean autoTx0Aggregate) {
    this.autoTx0Aggregate = autoTx0Aggregate;
  }

  public String getAutoTx0PoolId() {
    return autoTx0PoolId;
  }

  public void setAutoTx0PoolId(String autoTx0PoolId) {
    this.autoTx0PoolId = autoTx0PoolId;
  }

  public boolean isResync() {
    return resync;
  }

  public void setResync(boolean resync) {
    this.resync = resync;
  }

  @Override
  public Map<String, String> getConfigInfo() {
    Map<String, String> configInfo = super.getConfigInfo();

    configInfo.put("cli/version", Integer.toString(getVersion()));
    configInfo.put("cli/tor", Boolean.toString(getTor()));

    String apiKey = getApiKey();
    configInfo.put(
        "cli/apiKey",
        !org.apache.commons.lang3.StringUtils.isEmpty(apiKey)
            ? ClientUtils.maskString(apiKey)
            : "null");
    configInfo.put(
        "cli/proxy", getCliProxy().isPresent() ? getCliProxy().get().toString() : "null");
    configInfo.put("cli/autoTx0Aggregate", Boolean.toString(autoTx0Aggregate));
    configInfo.put("cli/autoTx0PoolId", autoTx0PoolId != null ? autoTx0PoolId : "null");
    configInfo.put("cli/resync", Boolean.toString(resync));
    return configInfo;
  }

  //

  public boolean isDojoEnabled() {
    return getDojo() != null && getDojo().isEnabled();
  }

  public Collection<HttpUsage> computeTorHttpUsages() {
    List<HttpUsage> httpUsages = new LinkedList<>();
    if (!getTor()) {
      // tor is disabled
      return httpUsages;
    }

    // backend
    if (getTorConfig().getBackend().isEnabled()) {
      httpUsages.add(HttpUsage.BACKEND);
    }

    // coordinator
    if (getTorConfig().getCoordinator().isEnabled()) {
      httpUsages.add(HttpUsage.COORDINATOR_WEBSOCKET);
      httpUsages.add(HttpUsage.COORDINATOR_REST);
      httpUsages.add(HttpUsage.COORDINATOR_REGISTER_OUTPUT);
    }
    return httpUsages;
  }
}
