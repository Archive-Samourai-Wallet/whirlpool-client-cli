package com.samourai.whirlpool.cli.wallet.data.dataPersister;

import com.samourai.whirlpool.cli.persistence.repository.UtxoConfigRepository;
import com.samourai.whirlpool.cli.persistence.repository.WalletStateRepository;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.data.dataPersister.AbstractDataPersisterFactory;

public class DbDataPersisterFactory extends AbstractDataPersisterFactory {
  private UtxoConfigRepository utxoConfigRepository;
  private WalletStateRepository walletStateRepository;

  public DbDataPersisterFactory(
      UtxoConfigRepository utxoConfigRepository, WalletStateRepository walletStateRepository) {
    super();
    this.utxoConfigRepository = utxoConfigRepository;
    this.walletStateRepository = walletStateRepository;
  }

  @Override
  protected WalletStatePersisterDb computeWalletStatePersister(WhirlpoolWallet whirlpoolWallet) {
    return new WalletStatePersisterDb(walletStateRepository);
  }

  @Override
  protected UtxoConfigPersisterDb computeUtxoConfigPersister(WhirlpoolWallet whirlpoolWallet) {
    return new UtxoConfigPersisterDb(utxoConfigRepository);
  }
}
