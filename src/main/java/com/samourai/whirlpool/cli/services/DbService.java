package com.samourai.whirlpool.cli.services;

import com.samourai.boltzmann.beans.BoltzmannResult;
import com.samourai.whirlpool.cli.persistence.beans.LogType;
import com.samourai.whirlpool.cli.persistence.entity.BoltzmannEntity;
import com.samourai.whirlpool.cli.persistence.entity.LogEntity;
import com.samourai.whirlpool.cli.persistence.repository.BoltzmannRepository;
import com.samourai.whirlpool.cli.persistence.repository.LogRepository;
import com.samourai.whirlpool.cli.persistence.repository.UtxoConfigRepository;
import com.samourai.whirlpool.cli.persistence.repository.WalletStateRepository;
import com.samourai.whirlpool.cli.utils.WalletRoutingDataSource;
import com.samourai.whirlpool.client.mix.handler.DestinationType;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DbService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private WalletRoutingDataSource walletRoutingDataSource;
  private BoltzmannRepository boltzmannRepository;
  private LogRepository logRepository;
  private UtxoConfigRepository utxoConfigRepository;
  private WalletStateRepository walletStateRepository;

  public DbService(
      WalletRoutingDataSource walletRoutingDataSource,
      BoltzmannRepository boltzmannRepository,
      LogRepository logRepository,
      UtxoConfigRepository utxoConfigRepository,
      WalletStateRepository walletStateRepository) {
    this.walletRoutingDataSource = walletRoutingDataSource;
    this.boltzmannRepository = boltzmannRepository;
    this.logRepository = logRepository;
    this.utxoConfigRepository = utxoConfigRepository;
    this.walletStateRepository = walletStateRepository;
  }

  // BOLTZMANN
  public BoltzmannEntity findBoltzmann(String txid) {
    return boltzmannRepository.getByTxid(txid);
  }

  public BoltzmannEntity createBoltzmann(String txid, BoltzmannResult boltzmannResult) {
    BoltzmannEntity boltzmannEntity = new BoltzmannEntity(txid, boltzmannResult);
    boltzmannRepository.save(boltzmannEntity);
    return boltzmannEntity;
  }

  // LOG
  public Collection<LogEntity> findLogs() {
    return logRepository.findAllByOrderByCreatedDesc();
  }

  public LogEntity createLog(
      long created,
      LogType logType,
      String data,
      Long amount,
      WhirlpoolAccount account,
      String fromHash,
      Integer fromIndex,
      String fromAddress,
      DestinationType destinationType,
      String toHash,
      Integer toIndex,
      String toAddress) {
    LogEntity logEntity =
        new LogEntity(
            created,
            logType,
            data,
            amount,
            account,
            fromHash,
            fromIndex,
            fromAddress,
            destinationType,
            toHash,
            toIndex,
            toAddress);
    logRepository.save(logEntity);
    return logEntity;
  }

  public String getDebug() {
    StringBuilder sb = new StringBuilder();
    try {
      Connection connection = walletRoutingDataSource.getDataSource().getConnection();
      sb.append(" • Connection: " + connection.getMetaData().getURL()).append("\n");
    } catch (Exception e) {
      log.error("", e);
    }
    sb.append(" • Tables:\n");
    sb.append("   - boltzmann: " + boltzmannRepository.count()).append("\n");
    sb.append("   - log: " + logRepository.count()).append("\n");
    sb.append("   - utxoConfig: " + utxoConfigRepository.count()).append("\n");
    sb.append("   - walletState: " + walletStateRepository.count()).append("\n");
    return sb.toString();
  }
}
