package com.samourai.whirlpool.cli.api;

import com.samourai.http.client.IHttpClient;
import com.samourai.wallet.util.oauth.OAuthManager;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiCliOpenWalletRequest;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiCliStateResponse;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiPoolsResponse;
import io.reactivex.Single;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientCliApi {
  private static Logger log = LoggerFactory.getLogger(ClientCliApi.class);

  private IHttpClient httpClient;
  private String urlCli;
  private OAuthManager oAuthManager;

  public ClientCliApi(IHttpClient httpClient, String urlCli, OAuthManager oAuthManager) {
    this.httpClient = httpClient;
    this.urlCli = urlCli;
    this.oAuthManager = oAuthManager;
    if (log.isDebugEnabled()) {
      log.debug("urlCli=" + urlCli + ", oAuthManager=" + (oAuthManager != null ? "yes" : "no"));
    }
  }

  public Single<Optional<ApiCliStateResponse>> openWallet(String seedPassphrase) throws Exception {
    String url = this.urlCli + CliApiEndpoint.REST_CLI_OPEN_WALLET;
    if (log.isDebugEnabled()) {
      log.debug("pools");
    }

    Map<String, String> headers = this.computeHeaders();
    ApiCliOpenWalletRequest request = new ApiCliOpenWalletRequest();
    request.seedPassphrase = seedPassphrase;
    return this.httpClient.postJson(url, ApiCliStateResponse.class, headers, request);
  }

  public ApiPoolsResponse pools() throws Exception {
    String url = this.urlCli + CliApiEndpoint.REST_POOLS;
    if (log.isDebugEnabled()) {
      log.debug("pools");
    }

    Map<String, String> headers = this.computeHeaders();
    ApiPoolsResponse response = this.httpClient.getJson(url, ApiPoolsResponse.class, headers, true);
    return response;
  }

  protected Map<String, String> computeHeaders() throws Exception {
    Map<String, String> headers = new HashMap();
    if (this.oAuthManager != null) {
      headers.put("Authorization", "Bearer " + this.oAuthManager.getOAuthAccessToken());
    }
    return headers;
  }

  protected IHttpClient getHttpClient() {
    return this.httpClient;
  }

  public String getUrlCli() {
    return this.urlCli;
  }
}
