package com.samourai.whirlpool.cli.api.controllers.rest.utxo;

import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoRef;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiTx0PreviewRequest;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiTx0PreviewResponse;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiTx0Request;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiTx0Response;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.tx0.*;
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
    String scode = whirlpoolWallet.getConfig().getScode();
    Tx0Info tx0Info = whirlpoolWallet.getWhirlpoolInfo().fetchTx0Info(scode);
    Tx0Config tx0Config = tx0Info.getTx0Config(payload.tx0FeeTarget, payload.mixFeeTarget);
    Tx0Previews tx0Previews = tx0Info.tx0Previews(whirlpoolUtxos, tx0Config);
    Tx0Preview tx0Preview = tx0Previews.getTx0Preview(payload.poolId);
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
    String scode = whirlpoolWallet.getConfig().getScode();
    Tx0Info tx0Info = whirlpoolWallet.getWhirlpoolInfo().fetchTx0Info(scode);
    Tx0Config tx0Config = tx0Info.getTx0Config(payload.tx0FeeTarget, payload.mixFeeTarget);
    Tx0 tx0 =
        tx0Info.tx0(
            whirlpoolWallet.getWalletSupplier(),
            whirlpoolWallet.getUtxoSupplier(),
            whirlpoolUtxos,
            pool,
            tx0Config);
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
