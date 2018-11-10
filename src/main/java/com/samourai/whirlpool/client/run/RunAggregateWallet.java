package com.samourai.whirlpool.client.run;

import com.samourai.api.SamouraiApi;
import com.samourai.api.beans.UnspentResponse;
import com.samourai.rpc.client.RpcClientService;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.whirlpool.client.tx0.TxAggregateService;
import com.samourai.whirlpool.client.utils.Bip84ApiWallet;
import com.samourai.whirlpool.client.utils.Bip84Wallet;
import com.samourai.whirlpool.client.utils.CliUtils;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunAggregateWallet {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int AGGREGATED_UTXOS_PER_TX = 500;
  protected static final Bech32UtilGeneric bech32Util = Bech32UtilGeneric.getInstance();

  private NetworkParameters params;
  private SamouraiApi samouraiApi;
  private Optional<RpcClientService> rpcClientService;
  private Bip84ApiWallet sourceWallet;
  private Bip84Wallet destinationWallet;

  public RunAggregateWallet(
      NetworkParameters params,
      SamouraiApi samouraiApi,
      Optional<RpcClientService> rpcClientService,
      Bip84ApiWallet sourceWallet) {
    this.params = params;
    this.samouraiApi = samouraiApi;
    this.rpcClientService = rpcClientService;
    this.sourceWallet = sourceWallet;
  }

  public boolean run(Bip84Wallet destinationWallet) throws Exception {
    return run(null, destinationWallet);
  }

  public boolean run(String destinationAddress) throws Exception {
    return run(destinationAddress, null);
  }

  private boolean run(String destinationAddress, Bip84Wallet destinationWallet) throws Exception {
    List<UnspentResponse.UnspentOutput> utxos = sourceWallet.fetchUtxos();
    if (utxos.isEmpty()) {
      // maybe you need to declare zpub as bip84 with /multiaddr?bip84=
      log.info("AggregateWallet result: no utxo to aggregate");
      return false;
    }
    if (log.isDebugEnabled()) {
      log.debug("Found " + utxos.size() + " utxo to aggregate:");
      CliUtils.printUtxos(utxos);
    }

    boolean success = false;
    int round = 0;
    int offset = 0;
    while (offset < utxos.size()) {
      List<UnspentResponse.UnspentOutput> subsetUtxos = new ArrayList<>();
      offset = AGGREGATED_UTXOS_PER_TX * round;
      for (int i = offset; i < (offset + AGGREGATED_UTXOS_PER_TX) && i < utxos.size(); i++) {
        subsetUtxos.add(utxos.get(i));
      }
      if (subsetUtxos.size() > 1) {
        String toAddress = destinationAddress;
        if (toAddress == null) {
          toAddress = bech32Util.toBech32(destinationWallet.getNextAddress(), params);
        }

        log.info("Aggregating " + subsetUtxos.size() + " utxos (pass #" + round + ")");
        runAggregate(subsetUtxos, toAddress);
        success = true;

        log.info("Refreshing utxos...");
        Thread.sleep(SamouraiApi.SLEEP_REFRESH_UTXOS);
      }
      round++;
    }
    return success;
  }

  private void runAggregate(List<UnspentResponse.UnspentOutput> postmixUtxos, String toAddress)
      throws Exception {
    List<TransactionOutPoint> spendFromOutPoints = new ArrayList<>();
    List<HD_Address> spendFromAddresses = new ArrayList<>();

    // spend
    for (UnspentResponse.UnspentOutput utxo : postmixUtxos) {
      spendFromOutPoints.add(utxo.computeOutpoint(params));
      spendFromAddresses.add(sourceWallet.getAddressAt(utxo));
    }

    int feeSatPerByte = samouraiApi.fetchFees();

    // tx
    Transaction txAggregate =
        new TxAggregateService(params)
            .txAggregate(spendFromOutPoints, spendFromAddresses, toAddress, feeSatPerByte);

    log.info("txAggregate:");
    log.info(txAggregate.toString());

    // broadcast
    log.info(" • Broadcasting TxAggregate...");
    CliUtils.broadcastOrNotify(rpcClientService, txAggregate);
  }
}
