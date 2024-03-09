package com.samourai.whirlpool.cli.services;

import com.samourai.wallet.api.backend.BackendServer;
import com.samourai.wallet.api.backend.websocket.BackendWsApi;
import com.samourai.wallet.httpClient.IHttpClientService;
import com.samourai.wallet.websocketClient.IWebsocketClient;
import com.samourai.websocket.client.JavaWebsocketClient;
import com.samourai.whirlpool.client.test.AbstractTest;
import java.lang.invoke.MethodHandles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BackendWsApiTest extends AbstractTest {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  @Autowired private IHttpClientService httpClientService;

  private static final String VPUB_1 =
      "vpub5YhvWhnmeNmCoSVYE38whMqRt1Cs4G9gExiadTDanMh6A4SCGh3KgtiGzSEXS2rhQgz1eTWcP9rLaWLedmLJnioWY6QWZ4t3ydnBqAwGpNt";

  private BackendWsApi backendWsApi;

  public BackendWsApiTest() throws Exception {}

  @BeforeEach
  @Override
  public void setup() throws Exception {
    super.setup();

    IWebsocketClient wsClient = new JavaWebsocketClient((JavaHttpClientService) httpClientService);
    backendWsApi = new BackendWsApi(wsClient, BackendServer.TESTNET.getBackendUrlClear(), null);
  }

  @Test
  public void subscribeBlock() throws Exception {
    backendWsApi.connect(
        foo -> {
          try {
            backendWsApi.subscribeBlock(
                message -> log.info("subscribeBlock.onMessage: " + message));
          } catch (Exception e) {
            log.error("", e);
          }
        },
        true);
    synchronized (this) {
      // wait(1500);
    }
  }

  @Test
  public void subscribeAddress() throws Exception {
    backendWsApi.connect(
        foo -> {
          try {
            backendWsApi.subscribeAddress(
                new String[] {VPUB_1},
                message -> log.info("subscribeAddress.onMessage: " + message));
          } catch (Exception e) {
            log.error("", e);
          }
        },
        true);
    synchronized (this) {
      // wait(1500);
    }
  }
}
