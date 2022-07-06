package com.samourai.whirlpool.cli.wallet;

import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.services.CliTorClientService;
import com.samourai.whirlpool.cli.services.JavaHttpClientService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.mix.MixParams;
import com.samourai.whirlpool.client.mix.listener.MixFailReason;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletConfig;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import com.samourai.whirlpool.client.wallet.data.dataSource.DojoDataSource;
import com.samourai.whirlpool.protocol.beans.Utxo;
import io.reactivex.Completable;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliWallet extends WhirlpoolWallet {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliConfig cliConfig;
  private CliConfigService cliConfigService;
  private CliTorClientService cliTorClientService;
  private JavaHttpClientService httpClientService;
  private String passphraseHash;

  public CliWallet(
      WhirlpoolWalletConfig config,
      byte[] seed,
      String passphrase,
      CliConfig cliConfig,
      CliConfigService cliConfigService,
      CliTorClientService cliTorClientService,
      JavaHttpClientService httpClientService)
      throws Exception {
    super(config, seed, passphrase);
    this.cliConfig = cliConfig;
    this.cliConfigService = cliConfigService;
    this.cliTorClientService = cliTorClientService;
    this.httpClientService = httpClientService;
    this.passphraseHash = computePassphraseHash(passphrase);
  }

  private String computePassphraseHash(String passphraseHash) {
    return CliUtils.sha512Hash(passphraseHash);
  }

  public boolean checkPassphraseHash(String hash) {
    return this.passphraseHash.equals(hash);
  }

  @Override
  public Completable startAsync() {
    if (!cliConfigService.isCliStatusReady()) {
      log.warn("Cannot start wallet: cliStatus is not ready");
      return Completable.error(
          new NotifiableException("Cannot start wallet: cliStatus is not ready"));
    }
    // start wallet
    return super.startAsync();
  }

  @Override
  public void stop() {
    super.stop();
  }

  @Override
  public void mix(WhirlpoolUtxo whirlpoolUtxo) throws NotifiableException {
    // get Tor ready before mixing
    cliTorClientService.waitReady();
    super.mix(whirlpoolUtxo);
  }

  @Override
  public void onMixSuccess(MixParams mixParams, Utxo receiveUtxo) {
    super.onMixSuccess(mixParams, receiveUtxo);

    // change http Tor identity
    if (cliConfig.getTor()) {
      httpClientService.changeIdentityRest();
    }
  }

  @Override
  public void onMixFail(MixParams mixParams, MixFailReason failReason, String notifiableError) {
    super.onMixFail(mixParams, failReason, notifiableError);

    // change http Tor identity
    if (cliConfig.getTor()) {
      httpClientService.changeIdentityRest();
    }
  }

  public void resyncMixsDone() {
    ((DojoDataSource) getDataSource()).resyncMixsDone();
  }

  @Override
  public void notifyError(String message) {
    CliUtils.notifyError(message);
  }

  // make public

  @Override
  public BipWallet getWalletDeposit() {
    return super.getWalletDeposit();
  }

  @Override
  public BipWallet getWalletPremix() {
    return super.getWalletPremix();
  }

  @Override
  public BipWallet getWalletPostmix() {
    return super.getWalletPostmix();
  }

  public BackendApi getBackendApi() {
    return ((DojoDataSource) getDataSource()).getBackendApi();
  }
}
