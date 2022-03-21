package com.samourai.whirlpool.cli.services;

import com.samourai.boltzmann.beans.BoltzmannResult;
import com.samourai.boltzmann.beans.BoltzmannSettings;
import com.samourai.boltzmann.beans.Txos;
import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.wallet.api.backend.beans.TxDetail;
import com.samourai.whirlpool.cli.persistence.entity.Boltzmann;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BoltzmannService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private DbService dbService;

  public BoltzmannService(DbService dbService) {
    this.dbService = dbService;
  }

  public Boltzmann getOrCompute(String txid, BackendApi backendApi) throws Exception {
    // find existing
    Boltzmann boltzmann = dbService.findBoltzmann(txid);
    if (boltzmann != null) {
      if (log.isDebugEnabled()) {
        log.debug("found existing result: " + txid);
      }
      return boltzmann;
    }

    // compute
    BoltzmannResult boltzmannResult = tx(txid, backendApi);

    // save
    boltzmann = dbService.createBoltzmann(txid, boltzmannResult);
    return boltzmann;
  }

  private Txos fetchTx(String txid, BackendApi backendApi) throws Exception {
    // fetch tx
    TxDetail tx = backendApi.fetchTx(txid, true);

    // build txos
    Txos txos = new Txos();
    for (TxDetail.TxInput input : tx.inputs) {
      txos.getInputs().put(input.outpoint.scriptpubkey, input.outpoint.value);
    }
    for (TxDetail.TxOutput output : tx.outputs) {
      txos.getOutputs().put(output.address, output.value);
    }
    return txos;
  }

  protected BoltzmannResult tx(String txid, BackendApi backendApi) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("processing: " + txid);
    }

    // fetch tx
    Txos txos = fetchTx(txid, backendApi);

    // configure and initialize Boltzmann
    BoltzmannSettings settings = new BoltzmannSettings();
    settings.setMaxCjIntrafeesRatio(0.005f);
    settings.setMaxTxos(12);
    settings.setMaxDuration(600);

    // initialize Boltzmann
    com.samourai.boltzmann.Boltzmann bolzmann = new com.samourai.boltzmann.Boltzmann(settings);

    // process
    BoltzmannResult result = bolzmann.process(txos);
    return result;
  }
}
