package com.samourai.xmanager.client;

import com.samourai.http.client.HttpUsage;
import com.samourai.http.client.JavaHttpClient;
import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.wallet.api.backend.BackendServer;
import com.samourai.wallet.api.backend.beans.MultiAddrResponse;
import com.samourai.wallet.api.backend.beans.UnspentResponse;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.test.AbstractTest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BackendApiTest extends AbstractTest {
  private static final long requestTimeout = 5000;

  private static final String VPUB_1 =
      "vpub5SLqN2bLY4WeYBwMrtdanr5SfhRC7AyW1aEwbtVbt7t9y6kgBCS6ajVA4LL7Jy2iojpH1hjaiTMp5h4y9dG2dC64bAk9ZwuFCX6AvxFddaa";
  private static final String VPUB_2 =
      "vpub5b14oTd3mpWGzbxkqgaESn4Pq1MkbLbzvWZju8Y6LiqsN9JXX7ZzvdCp1qDDxLqeHGr6BUssz2yFmUDm5Fp9jTdz4madyxK6mwgsCvYdK5S";

  private BackendApi backendApi;

  public BackendApiTest() throws Exception {
    HttpClient jettyHttpClient = CliUtils.computeHttpClient(Optional.empty(), "whirlpool-cli/test");
    JavaHttpClient httpClient =
        new JavaHttpClient(HttpUsage.BACKEND, jettyHttpClient, requestTimeout);
    backendApi =
        new BackendApi(
            httpClient, BackendServer.TESTNET.getBackendUrlClear(), java8.util.Optional.empty());
  }

  @Test
  public void initBip84() throws Exception {
    backendApi.initBip84(VPUB_1);
  }

  @Test
  public void fetchAddress() throws Exception {
    String zpub = VPUB_1;
    MultiAddrResponse.Address address = backendApi.fetchAddress(zpub);
    assertEquals(address, zpub, 63, 7, 0);
  }

  @Test
  public void fetchAddresses() throws Exception {
    String zpubs[] = {VPUB_1, VPUB_2};
    Map<String, MultiAddrResponse.Address> addresses = backendApi.fetchAddresses(zpubs);

    for (String zpub : zpubs) {
      Assertions.assertTrue(addresses.containsKey(zpub));
    }
    assertEquals(addresses.get(VPUB_1), VPUB_1, 63, 7, 0);
    assertEquals(addresses.get(VPUB_2), VPUB_2, 0, 0, 0);
  }

  @Test
  public void fetchUtxos() throws Exception {
    String zpub = VPUB_1;
    List<UnspentResponse.UnspentOutput> unspentOutputs = backendApi.fetchUtxos(zpub);
    Assertions.assertEquals(0, unspentOutputs.size());
  }

  @Test
  public void fetchUtxosMulti() throws Exception {
    String[] zpubs = new String[] {VPUB_1, VPUB_2};
    List<UnspentResponse.UnspentOutput> unspentOutputs = backendApi.fetchUtxos(zpubs);
    Assertions.assertEquals(0, unspentOutputs.size());
  }

  private void assertEquals(
      MultiAddrResponse.Address address,
      String zpub,
      int accountIndex,
      int changeIndex,
      int finalBalance) {
    Assertions.assertEquals(accountIndex, address.account_index);
    Assertions.assertEquals(changeIndex, address.change_index);
    Assertions.assertEquals(finalBalance, address.final_balance);
    Assertions.assertEquals(zpub, address.address);
  }
}
