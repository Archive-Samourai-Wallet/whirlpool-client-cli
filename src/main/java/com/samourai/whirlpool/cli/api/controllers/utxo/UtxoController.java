package com.samourai.whirlpool.cli.api.controllers.utxo;

import com.samourai.whirlpool.cli.api.controllers.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxo;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoRef;
import com.samourai.whirlpool.cli.api.protocol.rest.*;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.tx0.Tx0;
import com.samourai.whirlpool.client.tx0.Tx0Config;
import com.samourai.whirlpool.client.tx0.Tx0Preview;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import com.samourai.whirlpool.client.whirlpool.beans.Pool;
import java.util.LinkedList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class UtxoController extends AbstractRestController {
  @Autowired private CliWalletService cliWalletService;

  private WhirlpoolUtxo findUtxo(String utxoHash, int utxoIndex) throws Exception {
    // find utxo
    WhirlpoolUtxo whirlpoolUtxo =
        cliWalletService.getSessionWallet().getUtxoSupplier().findUtxo(utxoHash, utxoIndex);
    if (whirlpoolUtxo == null) {
      throw new NotifiableException("Utxo not found: " + utxoHash + ":" + utxoIndex);
    }
    return whirlpoolUtxo;
  }

  private List<WhirlpoolUtxo> findUtxos(ApiUtxoRef[] utxoRefs) throws Exception {
    List<WhirlpoolUtxo> whirlpoolUtos = new LinkedList<>();
    for (ApiUtxoRef utxoRef : utxoRefs) {
      // find utxo
      WhirlpoolUtxo whirlpoolUtxo = findUtxo(utxoRef.hash, utxoRef.index);
      if (whirlpoolUtxo == null) {
        throw new NotifiableException("Utxo not found: " + utxoRef.toString());
      }
      whirlpoolUtos.add(whirlpoolUtxo);
    }
    return whirlpoolUtos;
  }

  @RequestMapping(value = CliApiEndpoint.REST_UTXO_CONFIGURE, method = RequestMethod.POST)
  public ApiUtxo configureUtxo(
      @RequestHeader HttpHeaders headers,
      @PathVariable("hash") String utxoHash,
      @PathVariable("index") int utxoIndex,
      @Valid @RequestBody ApiUtxoConfigureRequest payload)
      throws Exception {
    checkHeaders(headers);

    // find utxo
    WhirlpoolUtxo whirlpoolUtxo = findUtxo(utxoHash, utxoIndex);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();

    // configure pool
    whirlpoolWallet.setPool(whirlpoolUtxo, payload.poolId);

    ApiUtxo apiUtxo = new ApiUtxo(whirlpoolUtxo);
    return apiUtxo;
  }

  @RequestMapping(value = CliApiEndpoint.REST_TX0_PREVIEW, method = RequestMethod.POST)
  public ApiTx0PreviewResponse tx0Preview(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiTx0PreviewRequest payload)
      throws Exception {
    checkHeaders(headers);

    // find utxo
    List<WhirlpoolUtxo> whirlpoolUtxos = findUtxos(payload.inputs);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();

    Pool pool = whirlpoolWallet.getPoolSupplier().findPoolById(payload.poolId);
    if (pool == null) {
      throw new NotifiableException("poolId is not valid");
    }

    // tx0 preview
    Tx0Config tx0Config = whirlpoolWallet.getTx0Config();
    Tx0Preview tx0Preview =
        whirlpoolWallet.tx0Preview(whirlpoolUtxos, pool, tx0Config, payload.feeTarget);
    return new ApiTx0PreviewResponse(tx0Preview);
  }

  @RequestMapping(value = CliApiEndpoint.REST_TX0, method = RequestMethod.POST)
  public ApiTx0Response tx0(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiTx0Request payload)
      throws Exception {
    checkHeaders(headers);

    // find utxo
    List<WhirlpoolUtxo> whirlpoolUtxos = findUtxos(payload.inputs);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();

    Pool pool = whirlpoolWallet.getPoolSupplier().findPoolById(payload.poolId);
    if (pool == null) {
      throw new NotifiableException("poolId is not valid");
    }

    // tx0
    Tx0Config tx0Config = whirlpoolWallet.getTx0Config();
    Tx0 tx0 = whirlpoolWallet.tx0(whirlpoolUtxos, pool, payload.feeTarget, tx0Config);
    return new ApiTx0Response(tx0);
  }

  @RequestMapping(value = CliApiEndpoint.REST_UTXO_STARTMIX, method = RequestMethod.POST)
  public void startMix(
      @RequestHeader HttpHeaders headers,
      @PathVariable("hash") String utxoHash,
      @PathVariable("index") int utxoIndex)
      throws Exception {
    checkHeaders(headers);

    // find utxo
    WhirlpoolUtxo whirlpoolUtxo = findUtxo(utxoHash, utxoIndex);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();

    // start mix
    whirlpoolWallet.mix(whirlpoolUtxo);
  }

  @RequestMapping(value = CliApiEndpoint.REST_UTXO_STOPMIX, method = RequestMethod.POST)
  public void stopMix(
      @RequestHeader HttpHeaders headers,
      @PathVariable("hash") String utxoHash,
      @PathVariable("index") int utxoIndex)
      throws Exception {
    checkHeaders(headers);

    // find utxo
    WhirlpoolUtxo whirlpoolUtxo = findUtxo(utxoHash, utxoIndex);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();

    // stop mix
    whirlpoolWallet.mixStop(whirlpoolUtxo);
  }
}
