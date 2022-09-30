package com.samourai.whirlpool.cli.api;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.samourai.http.client.HttpUsage;
import com.samourai.http.client.IHttpClient;
import com.samourai.wallet.util.oauth.OAuthApi;
import com.samourai.wallet.util.oauth.OAuthManager;
import com.samourai.wallet.util.oauth.OAuthManagerJava;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiCliStateResponse;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiPoolsResponse;
import com.samourai.whirlpool.cli.beans.CliStatus;
import com.samourai.whirlpool.client.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ClientCliApiTest extends AbstractTest {
  private ClientCliApi clientCliApi;

  private static final String SEED_PASSPHRASE = "test";

  @BeforeEach
  @Override
  public void setup() throws Exception {
    super.setup();

    clientCliApi = computeCliApi(cliConfig.getApiKey());
  }

  private ClientCliApi computeCliApi(String apiKey) {
    IHttpClient httpClient = httpClientService.getHttpClient(HttpUsage.BACKEND);
    String urlCli = "http://127.0.0.1:" + cliConfig.getApi().getHttpPort();

    // configure OAuth client
    OAuthApi cliOAuthApi = new CliOAuthApi(httpClient, urlCli);
    OAuthManager oAuthManager = new OAuthManagerJava(apiKey, cliOAuthApi);

    // client for CLI API
    return new ClientCliApi(httpClient, urlCli, oAuthManager);
  }

  @Test
  public void testCliAPI() throws Exception {
    // API: open wallet
    ApiCliStateResponse cliState =
        asyncUtil.blockingGet(clientCliApi.openWallet(SEED_PASSPHRASE)).get();
    Assertions.assertEquals(CliStatus.READY, cliState.getCliStatus());

    // API: request pools
    ApiPoolsResponse poolsResponse = clientCliApi.pools();
    Assertions.assertEquals(4, poolsResponse.getPools().size());
  }

  @Test
  public void testCliAPI_apiKeyInvalid() throws Exception {
    ClientCliApi clientCliApiInvalid = computeCliApi("invalidApiKey"); // invalid apiKey

    // API: open wallet
    try {
      // request should be rejected
      asyncUtil.blockingGet(clientCliApiInvalid.openWallet(SEED_PASSPHRASE)).get();
      Assertions.assertTrue(false);
    } catch (Exception e) {
      // allright, apiKey rejected
    }

    // API: request pools
    try {
      clientCliApiInvalid.pools(); // request should be rejected
      Assertions.assertTrue(false);
    } catch (Exception e) {
      // allright, apiKey rejected
    }
  }

  @Test
  public void testCliAPI_noApiKey() throws Exception {
    ClientCliApi clientCliApiInvalid = computeCliApi(null); // no apiKey

    // API: open wallet
    try {
      asyncUtil
          .blockingGet(clientCliApiInvalid.openWallet(SEED_PASSPHRASE))
          .get(); // request should be rejected
      Assertions.assertTrue(false);
    } catch (Exception e) {
      // allright, apiKey rejected
    }

    // API: request pools
    try {
      // request should be rejected
      clientCliApiInvalid.pools();
      Assertions.assertTrue(false);
    } catch (Exception e) {
      // allright, apiKey rejected
    }
  }
}
