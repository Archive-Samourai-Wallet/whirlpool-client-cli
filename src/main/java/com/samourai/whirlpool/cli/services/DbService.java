package com.samourai.whirlpool.cli.services;

import com.samourai.boltzmann.beans.BoltzmannResult;
import com.samourai.whirlpool.cli.persistence.beans.LogType;
import com.samourai.whirlpool.cli.persistence.entity.Boltzmann;
import com.samourai.whirlpool.cli.persistence.entity.Log;
import com.samourai.whirlpool.cli.persistence.repository.BoltzmannRepository;
import com.samourai.whirlpool.cli.persistence.repository.LogRepository;
import com.samourai.whirlpool.client.mix.handler.DestinationType;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DbService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private BoltzmannRepository boltzmannRepository;
  private LogRepository logRepository;

  public DbService(BoltzmannRepository boltzmannRepository, LogRepository logRepository) {
    this.boltzmannRepository = boltzmannRepository;
    this.logRepository = logRepository;
  }

  // BOLTZMANN
  public Boltzmann findBoltzmann(String txid) {
    return boltzmannRepository.getByTxid(txid);
  }

  public Boltzmann createBoltzmann(String txid, BoltzmannResult boltzmannResult) {
    Boltzmann boltzmann = new Boltzmann(txid, boltzmannResult);
    boltzmannRepository.save(boltzmann);
    return boltzmann;
  }

  // LOG
  public Collection<Log> findLogs() {
    return logRepository.findAllByOrderByCreatedDesc();
  }

  public Log createLog(
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
    Log log =
        new Log(
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
    // logRepository.save(log);
    return log;
  }
}
