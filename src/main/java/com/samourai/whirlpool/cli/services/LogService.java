package com.samourai.whirlpool.cli.services;

import com.google.common.eventbus.Subscribe;
import com.samourai.whirlpool.cli.persistence.beans.LogType;
import com.samourai.whirlpool.client.event.*;
import com.samourai.whirlpool.client.mix.handler.DestinationType;
import com.samourai.whirlpool.client.mix.handler.MixDestination;
import com.samourai.whirlpool.client.tx0.Tx0;
import com.samourai.whirlpool.client.wallet.WhirlpoolEventService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletService;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxoChanges;
import com.samourai.whirlpool.client.wallet.data.pool.PoolSupplier;
import com.samourai.whirlpool.client.whirlpool.beans.Pool;
import com.samourai.whirlpool.protocol.beans.Utxo;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private DbService dbService;
  private WhirlpoolWalletService whirlpoolWalletService;

  public LogService(DbService dbService, WhirlpoolWalletService whirlpoolWalletService) {
    this.dbService = dbService;
    this.whirlpoolWalletService = whirlpoolWalletService;
    WhirlpoolEventService.getInstance().register(this);
  }

  private PoolSupplier getPoolSupplier() {
    return whirlpoolWalletService.getWhirlpoolWallet().get().getPoolSupplier();
  }

  @Subscribe
  public void onTx0(Tx0Event e) {
    long created = System.currentTimeMillis();
    String data = null;
    Tx0 tx0 = e.getTx0();
    Long amount = tx0.getNbPremix() * tx0.getPremixValue();
    WhirlpoolAccount fromAccount = WhirlpoolAccount.DEPOSIT;
    String fromHash = null;
    Integer fromIndex = null;
    String fromAddress = null;

    DestinationType destinationType = DestinationType.PREMIX;
    String toHash = tx0.getTx().getHashAsString();
    Integer toIndex = null;
    String toAddress = null;
    dbService.createLog(
        created,
        LogType.TX0,
        data,
        amount,
        fromAccount,
        fromHash,
        fromIndex,
        fromAddress,
        destinationType,
        toHash,
        toIndex,
        toAddress);
  }

  @Subscribe
  public void onMixSuccess(MixSuccessEvent e) {
    createLog(e, e.getReceiveUtxo());
  }

  private void createLog(AbstractMixEvent e, Utxo receiveUtxo) {
    long created = System.currentTimeMillis();
    String data = null;
    Pool pool = getPoolSupplier().findPoolById(e.getMixProgress().getPoolId());
    Long amount = pool.getDenomination();
    WhirlpoolUtxo fromUtxo = e.getWhirlpoolUtxo();
    WhirlpoolAccount fromAccount = fromUtxo.getAccount();
    String fromHash = fromUtxo.getUtxo().tx_hash;
    Integer fromIndex = fromUtxo.getUtxo().tx_output_n;
    String fromAddress = fromUtxo.getUtxo().addr;

    String toHash = null;
    Integer toIndex = null;
    if (receiveUtxo != null) {
      toHash = receiveUtxo.getHash();
      toIndex = (int) receiveUtxo.getIndex();
    }

    MixDestination destination = e.getMixProgress().getDestination();
    DestinationType destinationType = null;
    String toAddress = null;
    if (destination != null) {
      destinationType = destination.getType();
      toAddress = destination.getAddress();
    }
    dbService.createLog(
        created,
        LogType.MIX_SUCCESS,
        data,
        amount,
        fromAccount,
        fromHash,
        fromIndex,
        fromAddress,
        destinationType,
        toHash,
        toIndex,
        toAddress);
  }

  @Subscribe
  public void onMixFail(MixFailEvent e) {
    createLog(e, null);
  }

  @Subscribe
  public void onUtxoChanges(UtxoChangesEvent e) {
    WhirlpoolUtxoChanges utxoChanges = e.getUtxoData().getUtxoChanges();
    Collection<WhirlpoolUtxo> utxosAdded = utxoChanges.getUtxosAdded();
    for (WhirlpoolUtxo whirlpoolUtxo : utxosAdded) {
      if (whirlpoolUtxo.getAccount() == WhirlpoolAccount.DEPOSIT) {
        long created = System.currentTimeMillis();
        String data = null;
        long amount = whirlpoolUtxo.getUtxo().value;
        WhirlpoolAccount fromAccount = null;
        String fromHash = null;
        Integer fromIndex = null;
        String fromAddress = null;

        DestinationType destinationType = DestinationType.find(whirlpoolUtxo.getAccount());
        String toHash = whirlpoolUtxo.getUtxo().tx_hash;
        Integer toIndex = whirlpoolUtxo.getUtxo().tx_output_n;
        String toAddress = whirlpoolUtxo.getUtxo().addr;
        dbService.createLog(
            created,
            LogType.RECEIVED,
            data,
            amount,
            fromAccount,
            fromHash,
            fromIndex,
            fromAddress,
            destinationType,
            toHash,
            toIndex,
            toAddress);
      }
    }
  }
}
