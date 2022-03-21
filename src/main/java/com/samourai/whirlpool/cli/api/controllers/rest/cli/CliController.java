package com.samourai.whirlpool.cli.api.controllers.rest.cli;

import com.samourai.wallet.api.pairing.PairingDojo;
import com.samourai.wallet.payload.BackupPayload;
import com.samourai.wallet.payload.PayloadUtilGeneric;
import com.samourai.wallet.util.AsyncUtil;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.whirlpool.cli.CliApplication;
import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.*;
import com.samourai.whirlpool.cli.beans.CliStatus;
import com.samourai.whirlpool.cli.beans.WhirlpoolPairingPayload;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.exception.CliRestartException;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import com.samourai.whirlpool.client.exception.NotifiableException;
import java.lang.invoke.MethodHandles;
import javax.validation.Valid;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class CliController extends AbstractRestController {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired private CliConfigService cliConfigService;
  @Autowired private CliWalletService cliWalletService;
  @Autowired private CliConfig cliConfig;

  @RequestMapping(value = CliApiEndpoint.REST_CLI, method = RequestMethod.GET)
  public ApiCliStateResponse state(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);
    ApiCliStateResponse response =
        new ApiCliStateResponse(cliWalletService.getCliState(), cliConfig);
    return response;
  }

  @RequestMapping(value = CliApiEndpoint.REST_CLI_INIT, method = RequestMethod.POST)
  public ApiCliInitResponse init(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiCliInitRequest payload)
      throws Exception {
    checkHeaders(headers);

    // security: check not already initialized
    if (!CliStatus.NOT_INITIALIZED.equals(cliConfigService.getCliStatus())) {
      throw new NotifiableException("CLI is already initialized.");
    }

    // pair
    String pairingPayload = payload.pairingPayload;
    boolean tor = payload.tor;
    boolean dojo = payload.dojo;
    WhirlpoolPairingPayload pairing = cliConfigService.parsePairingPayload(pairingPayload);

    // init
    String apiKey = cliConfigService.initialize(pairing, tor, dojo);
    ApiCliInitResponse response = new ApiCliInitResponse(apiKey);

    // restart CLI
    restartAfterReply();
    return response;
  }

  @RequestMapping(value = CliApiEndpoint.REST_CLI_CREATE, method = RequestMethod.POST)
  public ApiCliCreateResponse create(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiCliCreateRequest payload)
      throws Exception {
    checkHeaders(headers);

    // security: check not already initialized
    if (!CliStatus.NOT_INITIALIZED.equals(cliConfigService.getCliStatus())) {
      throw new NotifiableException("CLI is already initialized.");
    }

    // create
    boolean tor = payload.tor;
    boolean dojo = payload.dojo;
    String dojoUrl = payload.dojoUrl;
    String dojoApiKey = payload.dojoApiKey;
    String passphrase = payload.passphrase;
    boolean testnet = payload.testnet;
    PairingDojo pairingDojo =
        dojo ? cliConfigService.computePairingDojo(dojoUrl, dojoApiKey) : null;
    NetworkParameters params = FormatsUtilGeneric.getInstance().getNetworkParams(testnet);
    Pair<WhirlpoolPairingPayload, String> pair =
        cliConfigService.createWallet(pairingDojo, passphrase, params);
    WhirlpoolPairingPayload pairing = pair.getFirst();
    String mnemonic = pair.getSecond();

    // init
    String apiKey = cliConfigService.initialize(pairing, tor, dojo);
    ApiCliCreateResponse response = new ApiCliCreateResponse(apiKey, mnemonic, passphrase);

    // restart CLI
    restartAfterReply();
    return response;
  }

  @RequestMapping(value = CliApiEndpoint.REST_CLI_RESTORE_EXTERNAL, method = RequestMethod.POST)
  public ApiCliRestoreExternalResponse restoreExternal(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiCliRestoreExternalRequest payload)
      throws Exception {
    checkHeaders(headers);

    // security: check not already initialized
    if (!CliStatus.NOT_INITIALIZED.equals(cliConfigService.getCliStatus())) {
      throw new NotifiableException("CLI is already initialized.");
    }

    // restore
    boolean tor = payload.tor;
    boolean dojo = payload.dojo;
    String dojoUrl = payload.dojoUrl;
    String dojoApiKey = payload.dojoApiKey;
    String mnemonic = payload.mnemonic;
    String passphrase = payload.passphrase;
    boolean appendPassphrase = payload.appendPassphrase;
    boolean testnet = payload.testnet;
    PairingDojo pairingDojo =
        dojo ? cliConfigService.computePairingDojo(dojoUrl, dojoApiKey) : null;
    NetworkParameters params = FormatsUtilGeneric.getInstance().getNetworkParams(testnet);
    WhirlpoolPairingPayload pairing =
        cliConfigService.restoreExternal(
            pairingDojo, passphrase, params, mnemonic, appendPassphrase);

    // init
    String apiKey = cliConfigService.initialize(pairing, tor, dojo);
    ApiCliRestoreExternalResponse response = new ApiCliRestoreExternalResponse(apiKey);

    // restart CLI
    restartAfterReply();
    return response;
  }

  @RequestMapping(value = CliApiEndpoint.REST_CLI_RESTORE_BACKUP, method = RequestMethod.POST)
  public ApiCliRestoreBackupResponse restoreBackup(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiCliRestoreBackupRequest payload)
      throws Exception {
    checkHeaders(headers);

    // security: check not already initialized
    if (!CliStatus.NOT_INITIALIZED.equals(cliConfigService.getCliStatus())) {
      throw new NotifiableException("CLI is already initialized.");
    }

    // init
    boolean tor = payload.tor;
    boolean dojo = payload.dojo;
    String passphrase = payload.passphrase;
    String encryptedBackup = payload.backup;

    // restore
    BackupPayload backup;
    try {
      backup = PayloadUtilGeneric.getInstance().readBackup(encryptedBackup, passphrase);
    } catch (Exception e) {
      // forward error details
      throw new NotifiableException(e.getMessage());
    }
    PairingDojo pairingDojo = backup.computePairingDojo();
    WhirlpoolPairingPayload pairing =
        cliConfigService.restoreBackup(backup, pairingDojo, passphrase);

    // init
    String apiKey = cliConfigService.initialize(pairing, tor, dojo);
    ApiCliRestoreBackupResponse response = new ApiCliRestoreBackupResponse(apiKey);

    // restart CLI
    restartAfterReply();
    return response;
  }

  @RequestMapping(value = CliApiEndpoint.REST_CLI_OPEN_WALLET, method = RequestMethod.POST)
  public ApiCliStateResponse openWallet(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiCliOpenWalletRequest payload)
      throws Exception {
    checkHeaders(headers);

    try {
      AsyncUtil.getInstance()
          .blockingAwait(cliWalletService.openWallet(payload.seedPassphrase).startAsync());
    } catch (CliRestartException e) {
      // CLI upgrade success => restart
      CliApplication.restart();
    }

    // success
    return state(headers);
  }

  @RequestMapping(value = CliApiEndpoint.REST_CLI_CLOSE_WALLET, method = RequestMethod.POST)
  public ApiCliStateResponse closeWallet(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);

    cliWalletService.closeWallet();

    // success
    return state(headers);
  }

  @RequestMapping(value = CliApiEndpoint.REST_CLI_RESTART, method = RequestMethod.POST)
  public ApiCliStateResponse restart(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);

    CliApplication.restart();

    // success
    return state(headers);
  }

  @RequestMapping(value = CliApiEndpoint.REST_CLI_RESYNC, method = RequestMethod.POST)
  public void resync(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);

    // resync mix counters
    CliWallet cliWallet = cliWalletService.getSessionWallet();
    cliWallet.resyncMixsDone();
  }
}
