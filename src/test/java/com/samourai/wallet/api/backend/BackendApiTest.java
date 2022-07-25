package com.samourai.wallet.api.backend;

import com.samourai.http.client.HttpUsage;
import com.samourai.http.client.JavaHttpClient;
import com.samourai.wallet.api.backend.beans.MultiAddrResponse;
import com.samourai.wallet.api.backend.beans.TxDetail;
import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.api.backend.beans.WalletResponse;
import com.samourai.whirlpool.client.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BackendApiTest extends AbstractTest {
  private static final long requestTimeout = 5000;

  private static final String VPUB_1 =
      "vpub5SLqN2bLY4WeYBwMrtdanr5SfhRC7AyW1aEwbtVbt7t9y6kgBCS6ajVA4LL7Jy2iojpH1hjaiTMp5h4y9dG2dC64bAk9ZwuFCX6AvxFddaa";
  private static final String VPUB_2 =
      "vpub5b14oTd3mpWGzbxkqgaESn4Pq1MkbLbzvWZju8Y6LiqsN9JXX7ZzvdCp1qDDxLqeHGr6BUssz2yFmUDm5Fp9jTdz4madyxK6mwgsCvYdK5S";

  private BackendApi backendApi;

  public BackendApiTest() throws Exception {
    JavaHttpClient httpClient =
        new JavaHttpClient(requestTimeout, Optional.empty(), HttpUsage.BACKEND);
    backendApi = new BackendApi(httpClient, BackendServer.TESTNET.getBackendUrlClear(), null);
  }

  @Test
  public void initBip84() throws Exception {
    backendApi.initBip84(VPUB_1);
  }

  @Disabled
  @Test
  public void fetchAddress() throws Exception {
    String zpub = VPUB_1;
    MultiAddrResponse.Address address = backendApi.fetchAddress(zpub);
    assertAddressEquals(address, zpub, 63, 7, 0);
  }

  @Test
  public void fetchAddresses() throws Exception {
    String zpubs[] = {VPUB_1, VPUB_2};
    Map<String, MultiAddrResponse.Address> addresses = backendApi.fetchAddresses(zpubs);

    for (String zpub : zpubs) {
      Assertions.assertTrue(addresses.containsKey(zpub));
    }
    assertAddressEquals(addresses.get(VPUB_1), VPUB_1, 63, 7, 0);
    assertAddressEquals(addresses.get(VPUB_2), VPUB_2, 0, 0, 0);
  }

  @Test
  public void fetchUtxos() throws Exception {
    String zpub = VPUB_1;
    List<UnspentOutput> unspentOutputs = Arrays.asList(backendApi.fetchWallet(zpub).unspent_outputs);
    Assertions.assertEquals(0, unspentOutputs.size());
  }

  @Test
  public void fetchUtxosMulti() throws Exception {
    String[] zpubs = new String[] {VPUB_1, VPUB_2};
    List<UnspentOutput> unspentOutputs = Arrays.asList(backendApi.fetchWallet(zpubs).unspent_outputs);
    Assertions.assertEquals(0, unspentOutputs.size());
  }

  @Test
  public void fetchWallet() throws Exception {
    String[] zpubs = new String[] {VPUB_1, VPUB_2};
    WalletResponse walletResponse = backendApi.fetchWallet(zpubs);

    Assertions.assertEquals(0, walletResponse.unspent_outputs.length);

    Map<String, WalletResponse.Address> addressesMap = walletResponse.getAddressesMap();
    assertAddressEquals(addressesMap.get(VPUB_1), VPUB_1, 63, 7, 0);
    assertAddressEquals(addressesMap.get(VPUB_2), VPUB_2, 0, 0, 0);

    Assertions.assertTrue(walletResponse.txs.length > 0);

    Assertions.assertNotNull(walletResponse.info.latest_block.hash);
    Assertions.assertTrue(walletResponse.info.latest_block.height > 0);
    Assertions.assertTrue(walletResponse.info.latest_block.time > 0);
    for (MinerFeeTarget minerFeeTarget : MinerFeeTarget.values()) {
      Assertions.assertTrue(walletResponse.info.fees.get(minerFeeTarget.getValue()) > 0);
    }
  }

  @Test
  public void fetchTx() throws Exception {
    String txid = "0ba8c89afc51b65f133ac40131de7e170a41f87c5a4943502ff5705aae6341a8";
    TxDetail tx = backendApi.fetchTx(txid, true);

    Assertions.assertEquals(
        "0ba8c89afc51b65f133ac40131de7e170a41f87c5a4943502ff5705aae6341a8", tx.txid);
    Assertions.assertEquals(222, tx.size);
    Assertions.assertEquals(141, tx.vsize);
    Assertions.assertEquals(1, tx.version);
    Assertions.assertEquals(0, tx.locktime);

    Assertions.assertEquals(1, tx.inputs.length);
    Assertions.assertEquals(0, tx.inputs[0].n);
    Assertions.assertEquals(4294967295L, tx.inputs[0].seq);
    Assertions.assertEquals(
        "d60fae44ba8c728d43e7692c530b391eb393e298b169df3c09c150f79a66f1cc",
        tx.inputs[0].outpoint.txid);
    Assertions.assertEquals(1, tx.inputs[0].outpoint.vout);
    Assertions.assertEquals(238749293, tx.inputs[0].outpoint.value);
    Assertions.assertEquals(
        "0014ded4c3777ae40d686c981ee566a7021beda15ad1", tx.inputs[0].outpoint.scriptpubkey);

    Assertions.assertEquals(2, tx.outputs.length);
    Assertions.assertEquals(0, tx.outputs[0].n);
    Assertions.assertEquals(50000000, tx.outputs[0].value);
    Assertions.assertEquals(
        "001495df5bf26f2ae0307133ff6dc0a7d2e729872e89", tx.outputs[0].scriptpubkey);
    Assertions.assertEquals("witness_v0_keyhash", tx.outputs[0].type);
    Assertions.assertEquals("tb1qjh04hun09tsrqufnlakupf7juu5cwt5f87gh5u", tx.outputs[0].address);
  }

  private void assertAddressEquals(
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

  private void assertAddressEquals(
      WalletResponse.Address address,
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
