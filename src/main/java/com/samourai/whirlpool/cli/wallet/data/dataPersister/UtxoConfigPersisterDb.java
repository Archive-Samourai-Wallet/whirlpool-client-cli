package com.samourai.whirlpool.cli.wallet.data.dataPersister;

import com.samourai.whirlpool.cli.persistence.entity.UtxoConfigEntity;
import com.samourai.whirlpool.cli.persistence.repository.UtxoConfigRepository;
import com.samourai.whirlpool.client.wallet.data.supplier.IPersister;
import com.samourai.whirlpool.client.wallet.data.utxoConfig.UtxoConfigData;
import com.samourai.whirlpool.client.wallet.data.utxoConfig.UtxoConfigPersisted;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtxoConfigPersisterDb implements IPersister<UtxoConfigData> {
  private static final Logger log = LoggerFactory.getLogger(UtxoConfigPersisterDb.class);

  UtxoConfigRepository utxoConfigRepository;

  public UtxoConfigPersisterDb(UtxoConfigRepository utxoConfigRepository) {
    this.utxoConfigRepository = utxoConfigRepository;
  }

  @Override
  public UtxoConfigData read() throws Exception {
    Map<String, UtxoConfigPersisted> mapEntries = new LinkedHashMap<>();
    Iterable<UtxoConfigEntity> entries = utxoConfigRepository.findAll();
    entries.forEach(
        walletStateEntry ->
            mapEntries.put(walletStateEntry.id, walletStateEntry.toUtxoConfigPersisted()));
    return new UtxoConfigData(mapEntries);
  }

  @Override
  public void write(UtxoConfigData data) throws Exception {
    for (Map.Entry<String, UtxoConfigPersisted> e : data.getUtxoConfigs().entrySet()) {
      utxoConfigRepository.save(new UtxoConfigEntity(e.getKey(), e.getValue()));
    }
  }
}
