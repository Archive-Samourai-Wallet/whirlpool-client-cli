package com.samourai.whirlpool.cli.api;

import com.samourai.http.client.IHttpClient;
import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.wallet.api.backend.beans.RefreshTokenResponse;
import com.samourai.wallet.util.AsyncUtil;
import com.samourai.wallet.util.oauth.OAuthApi;
import com.samourai.whirlpool.cli.api.protocol.CliApi;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiAuthLoginRequest;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiAuthRefreshRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliOAuthApi implements OAuthApi {
  private Logger log = LoggerFactory.getLogger(BackendApi.class);
  private static final AsyncUtil asyncUtil = AsyncUtil.getInstance();
  private IHttpClient httpClient;
  private String urlCli;

  public CliOAuthApi(IHttpClient httpClient, String urlBackend) {
    this.httpClient = httpClient;
    this.urlCli = urlBackend;
  }

  private Map<String, String> computeHeaders() {
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put(CliApi.HEADER_API_VERSION, CliApi.API_VERSION);
    return headers;
  }

  @Override
  public RefreshTokenResponse.Authorization oAuthAuthenticate(String apiKey) throws Exception {
    String url = this.urlCli + "/rest/auth/login";
    if (this.log.isDebugEnabled()) {
      this.log.debug("tokenAuthenticate");
    }

    ApiAuthLoginRequest request = new ApiAuthLoginRequest();
    request.apiKey = apiKey;
    RefreshTokenResponse response =
        asyncUtil
            .blockingGet(
                this.httpClient.postJson(
                    url, RefreshTokenResponse.class, computeHeaders(), request))
            .get();
    if (response.authorizations != null
        && !StringUtils.isEmpty(response.authorizations.access_token)) {
      return response.authorizations;
    } else {
      throw new Exception("Authorization refused. Invalid apiKey?");
    }
  }

  @Override
  public String oAuthRefresh(String refreshTokenStr) throws Exception {
    String url = this.urlCli + "/rest/auth/refresh";
    if (this.log.isDebugEnabled()) {
      this.log.debug("tokenRefresh");
    }

    ApiAuthRefreshRequest request = new ApiAuthRefreshRequest();
    request.refreshToken = refreshTokenStr;
    RefreshTokenResponse response =
        asyncUtil
            .blockingGet(
                this.httpClient.postJson(
                    url, RefreshTokenResponse.class, computeHeaders(), request))
            .get();
    if (response.authorizations != null
        && !StringUtils.isEmpty(response.authorizations.access_token)) {
      return response.authorizations.access_token;
    } else {
      throw new Exception("Authorization refused. Invalid apiKey?");
    }
  }
}
