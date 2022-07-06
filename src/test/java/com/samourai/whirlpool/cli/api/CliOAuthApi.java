package com.samourai.whirlpool.cli.api;

import com.samourai.http.client.IHttpClient;
import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.wallet.api.backend.beans.RefreshTokenResponse;
import com.samourai.wallet.util.oauth.OAuthApi;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliOAuthApi implements OAuthApi {
  private Logger log = LoggerFactory.getLogger(BackendApi.class);
  private static final String URL_GET_AUTH_LOGIN = "/auth/login";
  private static final String URL_GET_AUTH_REFRESH = "/auth/refresh";
  private IHttpClient httpClient;
  private String urlCli;

  public CliOAuthApi(IHttpClient httpClient, String urlBackend) {
    this.httpClient = httpClient;
    this.urlCli = urlBackend;
  }

  @Override
  public RefreshTokenResponse.Authorization oAuthAuthenticate(String apiKey) throws Exception {
    String url = this.urlCli + "/rest/auth/login";
    if (this.log.isDebugEnabled()) {
      this.log.debug("tokenAuthenticate");
    }

    Map<String, String> postBody = new HashMap<>();
    postBody.put("apikey", apiKey);
    RefreshTokenResponse response =
        this.httpClient.postUrlEncoded(url, RefreshTokenResponse.class, null, postBody);
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

    Map<String, String> postBody = new HashMap();
    postBody.put("rt", refreshTokenStr);
    RefreshTokenResponse response =
        this.httpClient.postUrlEncoded(url, RefreshTokenResponse.class, null, postBody);
    if (response.authorizations != null
        && !StringUtils.isEmpty(response.authorizations.access_token)) {
      return response.authorizations.access_token;
    } else {
      throw new Exception("Authorization refused. Invalid apiKey?");
    }
  }
}
