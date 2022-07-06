package com.samourai.whirlpool.cli.api.controllers.rest.utxo;

import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoRef;
import com.samourai.whirlpool.cli.api.protocol.rest.*;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.services.WsNotifierService;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.tx0.Tx0;
import com.samourai.whirlpool.client.tx0.Tx0Config;
import com.samourai.whirlpool.client.tx0.Tx0Previews;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import com.samourai.whirlpool.client.whirlpool.beans.Pool;
import java.util.LinkedList;
import java.util.List;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class UtxoController extends AbstractRestController {
  @Autowired private CliWalletService cliWalletService;
  @Autowired private WsNotifierService wsNotifierService;

  private WhirlpoolUtxo findUtxo(ApiUtxoRef utxoRef) throws Exception {
    return findUtxo(utxoRef.hash, utxoRef.index);
  }

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

  @RequestMapping(value = CliApiEndpoint.REST_TX0_PREVIEWS, method = RequestMethod.POST)
  public ApiTx0PreviewsResponse tx0Preview(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiTx0PreviewRequest payload)
      throws Exception {
    checkHeaders(headers);

    // find utxo
    List<WhirlpoolUtxo> whirlpoolUtxos = findUtxos(payload.inputs);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();

    // tx0 preview
    Tx0Config tx0Config = whirlpoolWallet.getTx0Config(payload.tx0FeeTarget, payload.mixFeeTarget);
    Tx0Previews tx0Previews = whirlpoolWallet.tx0Previews(whirlpoolUtxos, tx0Config);
    return new ApiTx0PreviewsResponse(tx0Previews);
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
    Tx0Config tx0Config = whirlpoolWallet.getTx0Config(payload.tx0FeeTarget, payload.mixFeeTarget);
    Tx0 tx0 = whirlpoolWallet.tx0(whirlpoolUtxos, pool, tx0Config);
    return new ApiTx0Response(tx0);
  }

  @RequestMapping(value = CliApiEndpoint.REST_UTXO_SETMIX, method = RequestMethod.POST)
  public void setMix(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiUtxoSetMixRequest payload)
      throws Exception {
    checkHeaders(headers);

    // find utxo
    WhirlpoolUtxo whirlpoolUtxo = findUtxo(payload.utxo);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();

    if (payload.mix) {
      whirlpoolWallet.mix(whirlpoolUtxo);
    } else {
      whirlpoolWallet.mixStop(whirlpoolUtxo);
    }

    // notify utxos
    wsNotifierService.onUtxoChanges(whirlpoolWallet);
  }

  @RequestMapping(value = CliApiEndpoint.REST_UTXO_SETNOTE, method = RequestMethod.POST)
  public void setNote(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiUtxoSetNoteRequest payload)
      throws Exception {
    checkHeaders(headers);

    // find utxo
    WhirlpoolUtxo whirlpoolUtxo = findUtxo(payload.utxo);
    whirlpoolUtxo.setNote(StringUtils.isEmpty(payload.note) ? null : payload.note);

    // notify utxos
    wsNotifierService.onUtxoChanges((WhirlpoolWallet) null);
  }

  @RequestMapping(value = CliApiEndpoint.REST_UTXO_SETBLOCKED, method = RequestMethod.POST)
  public void setBlocked(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiUtxoSetBlockedRequest payload)
      throws Exception {
    checkHeaders(headers);

    // find utxo
    WhirlpoolUtxo whirlpoolUtxo = findUtxo(payload.utxo);
    whirlpoolUtxo.setBlocked(payload.blocked);

    // notify utxos
    wsNotifierService.onUtxoChanges((WhirlpoolWallet) null);
  }
}
