package com.samourai.whirlpool.cli.services;

import com.samourai.boltzmann.beans.BoltzmannResult;
import com.samourai.http.client.HttpUsage;
import com.samourai.http.client.JavaHttpClient;
import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.wallet.api.backend.BackendServer;
import com.samourai.whirlpool.client.test.AbstractTest;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BoltzmannServiceTest extends AbstractTest {
  private static final long requestTimeout = 5000;

  @Autowired private BoltzmannService boltzmannService;
  private BackendApi backendApi;

  public BoltzmannServiceTest() throws Exception {
    JavaHttpClient httpClient =
        new JavaHttpClient(requestTimeout, Optional.empty(), HttpUsage.BACKEND);
    backendApi =
        BackendApi.newBackendApiSamourai(httpClient, BackendServer.TESTNET.getBackendUrlClear());
  }

  @Test
  public void tx() throws Exception {
    String txid = "0ba8c89afc51b65f133ac40131de7e170a41f87c5a4943502ff5705aae6341a8";
    BoltzmannResult boltzmannResult = boltzmannService.tx(txid, backendApi);
    Assertions.assertTrue(true);
  }
}
