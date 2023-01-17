package com.samourai.whirlpool.cli.api.controllers.rest.utxo;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.*;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import com.samourai.whirlpool.client.wallet.data.utxo.UtxoSupplier;
import java.util.Collection;
import javax.validation.Valid;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class AddressController extends AbstractRestController {
  @Autowired private CliWalletService cliWalletService;
  @Autowired private CliConfig config;

  private ECKey findKeyByAddress(String address) throws Exception {
    UtxoSupplier utxoSupplier = cliWalletService.getSessionWallet().getUtxoSupplier();
    Collection<WhirlpoolUtxo> utxosByAddress = utxoSupplier.findUtxosByAddress(address);
    if (utxosByAddress.isEmpty()) {
      throw new NotifiableException("Address not found: " + address);
    }
    UnspentOutput utxo = utxosByAddress.iterator().next().getUtxo();
    ECKey key = ECKey.fromPrivate(utxoSupplier._getPrivKey(utxo.tx_hash, utxo.tx_output_n));
    return key;
  }

  @RequestMapping(value = CliApiEndpoint.REST_ADDRESS_SIGN, method = RequestMethod.POST)
  public ApiAddressSignResponse addressSign(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiAddressSignRequest payload)
      throws Exception {
    checkHeaders(headers);

    // sign
    ECKey ecKey = findKeyByAddress(payload.address);
    String signature = ecKey.signMessage(payload.message);
    return new ApiAddressSignResponse(signature);
  }

  @RequestMapping(value = CliApiEndpoint.REST_ADDRESS_PRIVATE, method = RequestMethod.POST)
  public ApiAddressPrivateResponse addressPrivate(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiAddressPrivateRequest payload)
      throws Exception {
    checkHeaders(headers);
    NetworkParameters params = config.getServer().getParams();

    // get private
    ECKey ecKey = findKeyByAddress(payload.address);
    String privateKey = ecKey.getPrivateKeyAsWiF(params);
    String redeemScript = new SegwitAddress(ecKey.getPubKey(), params).segwitRedeemScriptToString();
    return new ApiAddressPrivateResponse(privateKey, redeemScript);
  }
}
