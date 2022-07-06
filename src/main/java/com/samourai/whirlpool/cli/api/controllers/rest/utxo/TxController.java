package com.samourai.whirlpool.cli.api.controllers.rest.utxo;

import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiTxBoltzmannRequest;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiTxBoltzmannResponse;
import com.samourai.whirlpool.cli.persistence.entity.BoltzmannEntity;
import com.samourai.whirlpool.cli.services.BoltzmannService;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class TxController extends AbstractRestController {
  @Autowired private CliWalletService cliWalletService;
  @Autowired private BoltzmannService boltzmannService;

  @RequestMapping(value = CliApiEndpoint.REST_TX_BOLTZMANN, method = RequestMethod.POST)
  public ApiTxBoltzmannResponse boltzmann(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiTxBoltzmannRequest payload)
      throws Exception {
    checkHeaders(headers);

    CliWallet cliWallet = cliWalletService.getSessionWallet();

    // compute
    BackendApi backendApi = cliWallet.getBackendApi();
    BoltzmannEntity boltzmannEntity = boltzmannService.getOrCompute(payload.txid, backendApi);
    return new ApiTxBoltzmannResponse(boltzmannEntity);
  }
}
