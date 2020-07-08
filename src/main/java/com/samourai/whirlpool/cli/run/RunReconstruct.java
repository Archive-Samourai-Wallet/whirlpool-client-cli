package com.samourai.whirlpool.cli.run;

import com.samourai.wallet.api.backend.beans.TxsResponse;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunReconstruct {
  private static final int TXS_PER_PAGE = 1000;
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliWallet cliWallet;

  public RunReconstruct(CliWallet cliWallet) {
    this.cliWallet = cliWallet;
  }

  public void run() throws Exception {
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ RECONSTRUCT");
    log.info("⣿ This will reconstruct mix counters.");
    log.info("⣿ • Continue?");
    String userInput =
        CliUtils.readUserInputRequired("Continue? (y/n)", false, new String[] {"y", "n", "Y", "N"});
    if (!userInput.toLowerCase().equals("y")) {
      return;
    }
    reconstruct();
  }

  public void reconstruct() throws Exception {
    Collection<WhirlpoolUtxo> whirlpoolUtxos =
        cliWallet.getUtxoSupplier().findUtxos(WhirlpoolAccount.POSTMIX);

    log.info("Reconstructing mix counters...");

    Map<String, TxsResponse.Tx> txs = fetchTxs();

    int fixedUtxos = 0;
    for (WhirlpoolUtxo whirlpoolUtxo : whirlpoolUtxos) {
      int mixsDone = recount(whirlpoolUtxo, txs);
      if (mixsDone != whirlpoolUtxo.getMixsDone()) {
        log.info(
            "Fixed "
                + whirlpoolUtxo.getUtxo().tx_hash
                + ":"
                + whirlpoolUtxo.getUtxo().tx_output_n
                + ": "
                + whirlpoolUtxo.getMixsDone()
                + " => "
                + mixsDone);
        whirlpoolUtxo.setMixsDone(mixsDone);
        fixedUtxos++;
      }
    }
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ RECONSTRUCT SUCCESS");
    log.info("⣿ " + fixedUtxos + "/" + whirlpoolUtxos.size() + " utxos updated.");
    log.info(CliUtils.LOG_SEPARATOR);
  }

  private Map<String, TxsResponse.Tx> fetchTxs() throws Exception {
    Map<String, TxsResponse.Tx> txs = new LinkedHashMap<>();
    int page = -1;
    String[] zpubs = new String[] {cliWallet.getWalletPostmix().getZpub()};
    TxsResponse txsResponse;
    do {
      page++;
      txsResponse = cliWallet.getConfig().getBackendApi().fetchTxs(zpubs, page, TXS_PER_PAGE);
      if (txsResponse.txs != null) {
        for (TxsResponse.Tx tx : txsResponse.txs) {
          txs.put(tx.hash, tx);
        }
      }
      log.info("Fetching postmix history... " + txs.size() + "/" + txsResponse.n_tx);
    } while ((page * TXS_PER_PAGE) < txsResponse.n_tx);
    return txs;
  }

  private int recount(WhirlpoolUtxo whirlpoolUtxo, Map<String, TxsResponse.Tx> txs) {
    int mixsDone = 0;

    String txid = whirlpoolUtxo.getUtxo().tx_hash;
    while (true) {
      TxsResponse.Tx tx = txs.get(txid);
      mixsDone++;
      if (tx == null || tx.inputs == null || tx.inputs.length == 0) {
        return mixsDone;
      }
      txid = tx.inputs[0].prev_out.txid;
    }
  }
}
