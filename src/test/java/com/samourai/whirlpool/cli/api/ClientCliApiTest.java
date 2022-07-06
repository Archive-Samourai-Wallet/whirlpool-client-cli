package com.samourai.whirlpool.cli.api;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.samourai.http.client.HttpUsage;
import com.samourai.http.client.IHttpClient;
import com.samourai.http.client.IHttpClientService;
import com.samourai.wallet.util.oauth.OAuthApi;
import com.samourai.wallet.util.oauth.OAuthManager;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiCliStateResponse;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiPoolsResponse;
import com.samourai.whirlpool.cli.beans.CliStatus;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.client.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ClientCliApiTest extends AbstractTest {
  @Autowired private IHttpClientService httpClientService;
  @Autowired private CliConfig cliConfig;
  private ClientCliApi clientCliApi;

  private static final String SEED_PASSPHRASE = "test";

  @BeforeEach
  @Override
  public void setup() throws Exception {
    super.setup();

    clientCliApi = computeCliApi();
  }

  private ClientCliApi computeCliApi() {
    IHttpClient httpClient = httpClientService.getHttpClient(HttpUsage.BACKEND);
    String urlCli = "http://127.0.0.1:" + cliConfig.getApi().getHttpPort();
    String apiKey = cliConfig.getApiKey();

    // configure OAuth client
    OAuthApi cliOAuthApi = new CliOAuthApi(httpClient, urlCli);
    // OAuthManager oAuthManager = new OAuthManagerJava(apiKey, cliOAuthApi); // TODO uncomment when
    // OAuth ready
    OAuthManager oAuthManager = null;

    // client for CLI API
    return new ClientCliApi(httpClient, urlCli, apiKey, oAuthManager);
  }

  @Test
  public void testCliAPI() throws Exception {
    // API: open wallet
    ApiCliStateResponse cliState =
        asyncUtil.blockingSingle(clientCliApi.openWallet(SEED_PASSPHRASE)).get();
    Assertions.assertEquals(CliStatus.READY, cliState.getCliStatus());

    // API: request pools
    ApiPoolsResponse poolsResponse = clientCliApi.pools();
    Assertions.assertEquals(4, poolsResponse.getPools().size());
  }
}
