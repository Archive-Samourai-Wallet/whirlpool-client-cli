package com.samourai.whirlpool.cli.wallet.data.dataPersister;

import com.samourai.whirlpool.cli.persistence.entity.WalletStateEntity;
import com.samourai.whirlpool.cli.persistence.repository.WalletStateRepository;
import com.samourai.whirlpool.client.wallet.data.supplier.IPersister;
import com.samourai.whirlpool.client.wallet.data.walletState.WalletStateData;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletStatePersisterDb implements IPersister<WalletStateData> {
  private static final Logger log = LoggerFactory.getLogger(WalletStatePersisterDb.class);

  WalletStateRepository walletStateRepository;

  public WalletStatePersisterDb(WalletStateRepository walletStateRepository) {
    this.walletStateRepository = walletStateRepository;
  }

  @Override
  public WalletStateData read() throws Exception {
    Map<String, Integer> mapEntries = new LinkedHashMap<>();
    Iterable<WalletStateEntity> entries = walletStateRepository.findAll();
    entries.forEach(
        walletStateEntity -> mapEntries.put(walletStateEntity.id, walletStateEntity.valueInt));
    return new WalletStateData(mapEntries);
  }

  @Override
  public void write(WalletStateData data) throws Exception {
    for (Map.Entry<String, Integer> e : data.getItems().entrySet()) {
      walletStateRepository.save(new WalletStateEntity(e.getKey(), e.getValue()));
    }
  }
}
